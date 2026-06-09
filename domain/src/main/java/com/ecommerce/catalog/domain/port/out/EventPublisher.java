package com.ecommerce.catalog.domain.port.out;

import com.ecommerce.catalog.domain.event.DomainEvent;
import java.util.List;

/**
 * Puerto de salida para la publicación de eventos de dominio.
 * Permite que el dominio notifique cambios al resto del ecosistema (RabbitMQ, Kafka, etc.)
 */
public interface EventPublisher {
    void publish(DomainEvent event);
    
    default void publish(List<DomainEvent> events) {
        if (events != null) {
            events.forEach(this::publish);
        }
    }
}
