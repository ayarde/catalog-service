package com.ecommerce.catalog.infrastructure.config;

import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.catalog.infrastructure.adapter.out.messaging.RabbitConfig;
import com.ecommerce.catalog.infrastructure.adapter.out.messaging.RabbitEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;

import java.util.Map;

@Configuration
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        RedisAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class
})
@Import({RabbitConfig.class, RabbitEventPublisher.class})
public class InfrastructureRabbitIntTestConfig {

    public static final String TEST_QUEUE = "catalog.events.inttest";

    @Bean
    Queue intTestQueue() {
        return new Queue(TEST_QUEUE, false, true, true);
    }

    @Bean
    Binding intTestBinding(Queue intTestQueue, TopicExchange catalogExchange) {
        return BindingBuilder.bind(intTestQueue).to(catalogExchange).with("product_created");
    }

    @Bean
    @Primary
    MessageConverter intTestMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.ecommerce.catalog.domain.event");
        typeMapper.setIdClassMapping(Map.of(
                ProductCreatedEvent.class.getName(), ProductCreatedEvent.class
        ));
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
