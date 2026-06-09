package com.ecommerce.catalog.infrastructure.adapter.out.messaging;

import com.ecommerce.catalog.domain.event.DomainEvent;
import com.ecommerce.catalog.domain.port.out.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String routingKey = toRoutingKey(event.eventType());
            rabbitTemplate.convertAndSend(RabbitConfig.CATALOG_EXCHANGE, routingKey, event);
            log.debug("Published domain event {} with routing key {}", event.eventType(), routingKey);
        } catch (Exception ex) {
            log.error("Failed to publish domain event {}: {}", event.eventType(), ex.getMessage(), ex);
        }
    }

    static String toRoutingKey(String eventType) {
        return eventType.toLowerCase();
    }
}
