package com.ecommerce.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ProductCreatedEvent(
        UUID eventId,
        Instant occurredAt,
        Long aggregateId,
        String skuBase,
        String name
) implements DomainEvent {
    public ProductCreatedEvent(Long aggregateId, String skuBase, String name) {
        this(UUID.randomUUID(), Instant.now(), aggregateId, skuBase, name);
    }

    @Override
    public String eventType() {
        return "PRODUCT_CREATED";
    }
}
