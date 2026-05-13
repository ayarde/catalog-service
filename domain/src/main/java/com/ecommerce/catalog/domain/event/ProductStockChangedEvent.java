package com.ecommerce.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ProductStockChangedEvent(
        UUID eventId,
        Instant occurredAt,
        Long aggregateId,
        Long variantId,
        Integer newStock,
        String status
) implements DomainEvent {
    public ProductStockChangedEvent(Long aggregateId, Long variantId, Integer newStock, String status) {
        this(UUID.randomUUID(), Instant.now(), aggregateId, variantId, newStock, status);
    }

    @Override
    public String eventType() {
        return "PRODUCT_STOCK_CHANGED";
    }
}
