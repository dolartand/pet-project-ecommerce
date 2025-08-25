package com.ecommerce.backend.modules.inventory.service;

import com.ecommerce.backend.config.RabbitConfig;
import com.ecommerce.backend.modules.order.entity.Order;
import com.ecommerce.backend.modules.order.entity.OrderStatus;
import com.ecommerce.backend.shared.events.BaseEvent;
import com.ecommerce.backend.shared.events.OrderCreatedEvent;
import com.ecommerce.backend.shared.events.OrderStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        BaseEvent baseEvent = objectMapper.readValue(eventJson, BaseEvent.class);

        if (!isEventAlreadyProcessed(baseEvent.getEventId())) {
            log.info("Received event {} for inventory processing.", baseEvent.getEventId());

            if (baseEvent.getEventType().equals(OrderCreatedEvent.class.getSimpleName())) {
                OrderCreatedEvent event = objectMapper.readValue(eventJson, OrderCreatedEvent.class);
                processOrderCreation(event.getOrder());
            } else if (baseEvent.getEventType().equals(OrderStatusChangedEvent.class.getSimpleName())) {
                OrderStatusChangedEvent event = objectMapper.readValue(eventJson, OrderStatusChangedEvent.class);
                processOrderStatusChange(event.getOrder());
            } else {
                log.warn("Unknown event type received in inventory listener: {}", baseEvent.getEventType());
            }
        } else {
            log.warn("Event {} already processed. Skipping.", baseEvent.getEventId());
        }
    }

    private void processOrderCreation(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            Map<Long, Integer> productQuantities = order.getItems().stream()
                    .collect(Collectors.toMap(item -> item.getProductId(), item -> item.getQuantity()));
            inventoryService.reserveProduct(order.getId(), productQuantities, order.getUser().getEmail());
        }
    }

    private void processOrderStatusChange(Order order) {
        String userEmail = order.getUser().getEmail();
        if (order.getStatus() == OrderStatus.CANCELLED) {
            inventoryService.cancelReservation(order.getId(), userEmail);
        } else if (order.getStatus() == OrderStatus.SHIPPED) {
            inventoryService.confirmReservation(order.getId(), userEmail);
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
