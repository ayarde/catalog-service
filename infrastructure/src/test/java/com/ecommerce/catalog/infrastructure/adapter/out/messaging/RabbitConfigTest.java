package com.ecommerce.catalog.infrastructure.adapter.out.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigTest {

    private final RabbitConfig rabbitConfig = new RabbitConfig();

    /**
    En esta prueba unitaria se espera que se valide la creación del exchange de catálogo.

    Características extras:
    - Utiliza la configuración por defecto del exchange.
    - No requiere dependencias externas.

    Se espera que el método:
    - Devuelva un TopicExchange con el nombre configurado en la clase.
    **/
    @Test
    void catalogExchange_WhenCreated_ShouldUseConfiguredName() {
        //when
        TopicExchange exchange = rabbitConfig.catalogExchange();

        //then
        assertThat(exchange.getName()).isEqualTo(RabbitConfig.CATALOG_EXCHANGE);
    }

    /**
    En esta prueba unitaria se espera que se valide el convertidor de mensajes JSON.

    Características extras:
    - Utiliza la clase Jackson2JsonMessageConverter.
    - Configura el convertidor para manejar tipos genéricos.

    Se espera que el método:
    - Retorne una instancia de Jackson2JsonMessageConverter.
    **/
    @Test
    void messageConverter_WhenCreated_ShouldBeJackson() {
        //when
        MessageConverter converter = rabbitConfig.messageConverter();

        //then
        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
