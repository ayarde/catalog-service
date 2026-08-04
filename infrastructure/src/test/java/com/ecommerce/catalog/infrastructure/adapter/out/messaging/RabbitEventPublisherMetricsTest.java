package com.ecommerce.catalog.infrastructure.adapter.out.messaging;

import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherMetricsTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private MeterRegistry meterRegistry;
    private RabbitEventPublisher publisher;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        publisher = new RabbitEventPublisher(rabbitTemplate, meterRegistry);
    }

    /**
    En esta prueba unitaria se espera que se incremente el contador de eventos fallidos
    cuando el broker de RabbitMQ no está disponible.

    Características extras:
    - El broker RabbitMQ lanza una excepción al publicar el evento.
    - El evento de dominio es un ProductCreatedEvent.

    Se espera que el método:
    - Capture la excepción sin propagarla.
    - Incremente el contador catalog_events_publish_failed_total en 1.
    **/
    @Test
    void publish_WhenBrokerUnavailable_IncrementsPublishFailedCounter() {
        //given
        ProductCreatedEvent event = new ProductCreatedEvent(1L, "SKU", "Name");
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        //when
        publisher.publish(event);

        //then
        double count = meterRegistry.get("catalog_events_publish_failed_total").counter().count();
        assertThat(count).isEqualTo(1);
    }
}
