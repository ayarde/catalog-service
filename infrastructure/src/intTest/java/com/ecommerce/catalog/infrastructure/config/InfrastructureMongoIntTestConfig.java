package com.ecommerce.catalog.infrastructure.config;

import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.repository.SpringDataMongoProductRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;

@Configuration
@EnableAutoConfiguration(exclude = {
        RabbitAutoConfiguration.class,
        RedisAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class
})
@EnableMongoRepositories(basePackageClasses = SpringDataMongoProductRepository.class)
@ComponentScan(basePackages = "com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb")
public class InfrastructureMongoIntTestConfig {
}
