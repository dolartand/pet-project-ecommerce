package com.ecommerce.backend.shared.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) {
            return;
        }

        log.info("Processing {} events from outbox", events.size());

        for (OutboxEvent event : events) {
            try {
                rabbitTemplate.convertAndSend(event.getExchangeName(), event.getRoutingKey(), event.getPayload());
                event.setProcessed(true);
                event.setProcessedAt(LocalDateTime.now());
                event.setErrorMessage(null);
            } catch (Exception e) {
                log.error("Failed to publish event {}. Error: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());
                if (event.getRetryCount() >= event.getMaxRetries()) {
                    log.error("Event {} has reached the maximum number of retries", event.getId());
                    event.setProcessed(true);
                }
            }

            outboxEventRepository.save(event);
        }
    }
}
