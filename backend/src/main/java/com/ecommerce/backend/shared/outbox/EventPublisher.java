package com.ecommerce.backend.shared.outbox;

import com.ecommerce.backend.shared.events.BaseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(BaseEvent event, String exchangeName, String routingKey) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(event.getEventId())
                    .aggregateId(event.getAggregateId())
                    .eventType(event.getEventType())
                    .payload(payload)
                    .exchangeName(exchangeName)
                    .routingKey(routingKey)
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .maxRetries(3)
                    .build();

            outboxEventRepository.save(outboxEvent);
            log.info("Event {} of type {} published to outbox for aggregate {}",
                    event.getEventId(), event.getEventType(), event.getAggregateId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload for event {}", event.getEventId(), e);
            throw new RuntimeException("Failed to serialize event" ,e);
        }
    }
}
