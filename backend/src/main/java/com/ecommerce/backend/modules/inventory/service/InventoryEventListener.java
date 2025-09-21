package com.ecommerce.backend.modules.inventory.service;

import com.ecommerce.backend.config.RabbitConfig;
import com.ecommerce.backend.modules.order.entity.Order;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import com.ecommerce.backend.shared.events.BaseEvent;
import com.ecommerce.backend.shared.events.OrderCreatedEvent;
import com.ecommerce.backend.shared.events.OrderStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    private final Set<String> processedEventIds = Collections.synchronizedSet(new HashSet<>());

    @RabbitListener(queues = RabbitConfig.INVENTORY_EVENTS_QUEUE)
    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handleOrderEvent(@Payload String eventJson) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(eventJson);
        String eventType = rootNode.get("eventType").asText();
        String eventId = rootNode.get("eventId").asText();

        if (!isEventAlreadyProcessed(eventId)) {
            log.info("Received event {} for inventory processing.", eventId);

            if (eventType.equals(OrderCreatedEvent.class.getSimpleName())) {
                OrderCreatedEvent event = objectMapper.readValue(eventJson, OrderCreatedEvent.class);
                processOrderCreation(event.getOrder());
            } else if (eventType.equals(OrderStatusChangedEvent.class.getSimpleName())) {
                OrderStatusChangedEvent event = objectMapper.readValue(eventJson, OrderStatusChangedEvent.class);
                processOrderStatusChange(event.getOrder());
            } else {
                log.warn("Unknown event type received in inventory listener: {}", eventType);
            }
        } else {
            log.warn("Event {} already processed. Skipping.", eventId);
        }
    }

    private void processOrderCreation(Order order) {
        log.info("Processing order creation for order id: {}", order.getId());
        if (order.getStatus() == OrderStatus.PENDING) {
            Map<Long, Integer> productQuantities = order.getItems().stream()
                    .collect(Collectors.toMap(item -> item.getProductId(), item -> item.getQuantity()));
            log.info("Reserving products for order id: {}. Products: {}", order.getId(), productQuantities);
            inventoryService.reserveProduct(order.getId(), productQuantities, order.getUser().getEmail());
            log.info("Successfully reserved products for order id: {}", order.getId());
        }
    }

    private void processOrderStatusChange(Order order) {
        log.info("Processing order status change for order id: {}. New status: {}", order.getId(), order.getStatus());
        String userEmail = order.getUser().getEmail();
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.info("Cancelling reservation for order id: {}", order.getId());
            inventoryService.cancelReservation(order.getId(), userEmail);
            log.info("Successfully cancelled reservation for order id: {}", order.getId());
        } else if (order.getStatus() == OrderStatus.SHIPPED) {
            log.info("Confirming reservation for order id: {}", order.getId());
            inventoryService.confirmReservation(order.getId(), userEmail);
            log.info("Successfully confirmed reservation for order id: {}", order.getId());
        }
    }

    @Recover
    public void recover(Exception e, String eventJson) {
        log.error("All retries failed for event. Moving to DLQ. Error: {}, Event: {}", e.getMessage(), eventJson);
        throw new AmqpRejectAndDontRequeueException("Event processing failed permanently.", e);
    }

    private boolean isEventAlreadyProcessed(String eventId) {
        return !processedEventIds.add(eventId);
    }
}
