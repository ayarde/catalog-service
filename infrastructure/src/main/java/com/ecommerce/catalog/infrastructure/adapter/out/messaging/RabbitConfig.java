package com.ecommerce.catalog.infrastructure.adapter.out.messaging;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.TopicExchange;

@Configuration
public class RabbitConfig {

    public static final String CATALOG_EXCHANGE = "catalog.exchange";

    @Bean
    TopicExchange catalogExchange() {
        return new TopicExchange(CATALOG_EXCHANGE);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
