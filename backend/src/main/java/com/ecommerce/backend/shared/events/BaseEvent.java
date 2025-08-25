package com.ecommerce.backend.shared.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BaseEvent {
    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String aggregateId;
    private Integer version = 1;

    public BaseEvent(String aggregateId) {
        this.aggregateId = aggregateId;
        this.eventType = this.getClass().getSimpleName();
    }
}
