package com.ecommerce.catalog.infrastructure.adapter.out.messaging;

import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitEventPublisher publisher;

    /**
    En esta prueba unitaria se espera que se valide la publicación exitosa en el exchange de catálogo.

    Características extras:
    - Simula la disponibilidad del broker RabbitMQ.
    - Utiliza un evento de producto recién creado.

    Se espera que el método:
    - Llame a RabbitTemplate.convertAndSend con el exchange y routing key correctos.
    - No lance ninguna excepción.
    **/
    @Test
    void publish_WhenBrokerAvailable_ShouldSendToExchange() {
        //given
        ProductCreatedEvent event = new ProductCreatedEvent(1L, "SKU", "Name");

        //when
        publisher.publish(event);

        //then
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.CATALOG_EXCHANGE),
                eq("product_created"),
                eq(event)
        );
    }

    /**
    En esta prueba unitaria se espera que se valide que un fallo del broker no propague la excepción.

    Características extras:
    - Simula una excepción RuntimeException del broker.
    - Verifica que el método capture y suprima la excepción.

    Se espera que el método:
    - No lance ninguna excepción al llamarse.
    - No devuelva valor (void).
    **/
    @Test
    void publish_WhenBrokerUnavailable_HandlesGracefully() {
        //given
        ProductCreatedEvent event = new ProductCreatedEvent(2L, "SKU-2", "Name 2");
        doThrow(new RuntimeException("broker down"))
                .when(rabbitTemplate)
                .convertAndSend(eq(RabbitConfig.CATALOG_EXCHANGE), eq("product_created"), eq(event));

        //when / then (no exception)
        publisher.publish(event);
    }

    /**
    En esta prueba unitaria se espera que se valide la conversión de tipo de evento a routing key.

    Características extras:
    - Convierte el nombre del evento a snake_case lower.
    - No depende de infraestructura externa.

    Se espera que el método:
    - Retorne la cadena "product_created" para el evento PRODUCT_CREATED.
    **/
    @Test
    void toRoutingKey_WhenProductCreated_ReturnsSnakeCaseLower() {
        //when
        String routingKey = RabbitEventPublisher.toRoutingKey("PRODUCT_CREATED");

        //then
        assertThat(routingKey).isEqualTo("product_created");
    }
}
