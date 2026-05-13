package com.ecommerce.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Interfaz base sellada para todos los eventos del dominio de Catálogo.
 * El uso de 'sealed' garantiza que solo los eventos definidos aquí puedan existir.
 */
public sealed interface DomainEvent 
    permits ProductCreatedEvent, ProductActivatedEvent, ProductPriceChangedEvent, ProductStockChangedEvent {
    
    UUID eventId();
    Instant occurredAt();
    Long aggregateId();
    String eventType();
}
