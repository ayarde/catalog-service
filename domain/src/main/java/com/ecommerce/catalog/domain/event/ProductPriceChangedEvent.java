package com.ecommerce.catalog.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductPriceChangedEvent(
        UUID eventId,
        Instant occurredAt,
        Long aggregateId,
        BigDecimal newPrice,
        String currency
) implements DomainEvent {
    public ProductPriceChangedEvent(Long aggregateId, BigDecimal newPrice, String currency) {
        this(UUID.randomUUID(), Instant.now(), aggregateId, newPrice, currency);
    }

    @Override
    public String eventType() {
        return "PRODUCT_PRICE_CHANGED";
    }
}
