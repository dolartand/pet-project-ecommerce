package com.ecommerce.backend.modules.notification.service;

import com.ecommerce.backend.config.RabbitConfig;
import com.ecommerce.backend.shared.events.BaseEvent;
import com.ecommerce.backend.shared.events.CartAbandonedEvent;
import com.ecommerce.backend.shared.events.OrderCreatedEvent;
import com.ecommerce.backend.shared.events.OrderStatusChangedEvent;
import com.ecommerce.backend.shared.events.UserRegisteredEvent;
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
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper;
    private final Set<String> processedEventIds = Collections.synchronizedSet(new HashSet<>());

    @RabbitListener(queues = RabbitConfig.USER_REGISTRATION_NOTIFICATIONS_QUEUE)
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void handleUserRegistration(@Payload String eventJson) throws JsonProcessingException {
        log.info("Received user registration event: {}", eventJson);
        UserRegisteredEvent event = deserializeEvent(eventJson, UserRegisteredEvent.class);
        if (isEventAlreadyProcessed(event.getEventId())) {
            log.warn("User registration event {} already processed. Skipping.", event.getEventId());
            return;
        }

        log.info("Sending registration confirmation for user: {}", event.getUserEmail());
        log.info("Successfully sent registration confirmation for user: {}", event.getUserEmail());
    }

    @RabbitListener(queues = RabbitConfig.ORDER_NOTIFICATIONS_QUEUE)
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void handleOrderEvent(@Payload String eventJson) throws JsonProcessingException {
        log.info("Received order event: {}", eventJson);
        JsonNode rootNode = objectMapper.readTree(eventJson);
        String eventType = rootNode.get("eventType").asText();
        String eventId = rootNode.get("eventId").asText();

        if (isEventAlreadyProcessed(eventId)) {
            log.warn("Order event {} already processed. Skipping.", eventId);
            return;
        }

        if (eventType.equals(OrderCreatedEvent.class.getSimpleName())) {
            OrderCreatedEvent event = objectMapper.readValue(eventJson, OrderCreatedEvent.class);
            log.info("Sending order creation notification for order ID: {}", event.getAggregateId());
            log.info("Successfully sent order creation notification for order ID: {}", event.getAggregateId());
        } else if (eventType.equals(OrderStatusChangedEvent.class.getSimpleName())) {
            OrderStatusChangedEvent event = objectMapper.readValue(eventJson, OrderStatusChangedEvent.class);
            log.info("Sending order status change notification for order ID: {}. New status: {}", event.getAggregateId(), event.getOrder().getStatus());
            log.info("Successfully sent order status change notification for order ID: {}", event.getAggregateId());
        }
    }

    @RabbitListener(queues = RabbitConfig.CART_ABANDONED_NOTIFICATIONS_QUEUE)
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void handleAbandonedCartEvent(@Payload String eventJson) throws JsonProcessingException {
        log.info("Received abandoned cart event: {}", eventJson);
        CartAbandonedEvent event = deserializeEvent(eventJson, CartAbandonedEvent.class);
        if (isEventAlreadyProcessed(event.getEventId())) {
            log.warn("Cart abandoned event {} already processed. Skipping.", event.getEventId());
            return;
        }

        log.info("Sending abandoned cart reminder for cart ID: {}", event.getAggregateId());
        log.info("Successfully sent abandoned cart reminder for cart ID: {}", event.getAggregateId());
    }

    @Recover
    public void recover(Exception e, String eventJson) {
        log.error("All retries failed for notification event. Moving to DLQ. Error: {}, Event: {}", e.getMessage(), eventJson);
        throw new AmqpRejectAndDontRequeueException("Notification processing failed permanently.", e);
    }

    private <T extends BaseEvent> T deserializeEvent(String json, Class<T> eventType) throws JsonProcessingException {
        try {
            return objectMapper.readValue(json, eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize event JSON: {}", json, e);
            throw e;
        }
    }

    private boolean isEventAlreadyProcessed(String eventId) {
        return !processedEventIds.add(eventId);
    }
}
