package com.ecommerce.catalog.infrastructure.adapter.out.messaging;

import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.catalog.infrastructure.config.InfrastructureRabbitIntTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("inttest")
@SpringBootTest(classes = InfrastructureRabbitIntTestConfig.class)
class RabbitEventPublisherIntTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12-management-alpine");

    @Autowired
    private RabbitEventPublisher publisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Queue intTestQueue;

    /**
    En esta prueba de integración se valida la publicación real de un evento de dominio en RabbitMQ.

    Características extras:
    - Broker levantado con Testcontainers
    - Cola de verificación declarada vía configuración de test

    Se espera que el método:
    - Publique el mensaje en el exchange configurado
    - El mensaje llegue a la cola enlazada con routing key product_created
    **/
    @Test
    void publish_Success_MessageArrivesInQueue() {
        //given
        ProductCreatedEvent event = new ProductCreatedEvent(42L, "SKU-RABBIT", "Rabbit Product");
        String queueName = intTestQueue.getName();

        //when
        publisher.publish(event);
        Object received = rabbitTemplate.receiveAndConvert(queueName, 5000);

        //then
        assertThat(received).isNotNull();
        assertProductCreatedPayload(received, 42L, "SKU-RABBIT");
    }

    //<editor-fold desc="Métodos auxiliares">
    private void assertProductCreatedPayload(Object received, Long aggregateId, String skuBase) {
        if (received instanceof ProductCreatedEvent receivedEvent) {
            assertThat(receivedEvent.aggregateId()).isEqualTo(aggregateId);
            assertThat(receivedEvent.skuBase()).isEqualTo(skuBase);
            return;
        }
        if (received instanceof Map<?, ?> payload) {
            assertThat(payload.get("aggregateId")).isEqualTo(aggregateId);
            assertThat(payload.get("skuBase")).isEqualTo(skuBase);
            return;
        }
        fail("Tipo de payload inesperado: " + received.getClass().getName());
    }
    //</editor-fold>
}
