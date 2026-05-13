package com.ecommerce.catalog.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductActivatedEvent(
        UUID eventId,
        Instant occurredAt,
        Long aggregateId,
        String slug,
        BigDecimal basePrice
) implements DomainEvent {
    public ProductActivatedEvent(Long aggregateId, String slug, BigDecimal basePrice) {
        this(UUID.randomUUID(), Instant.now(), aggregateId, slug, basePrice);
    }

    @Override
    public String eventType() {
        return "PRODUCT_ACTIVATED";
    }
}
