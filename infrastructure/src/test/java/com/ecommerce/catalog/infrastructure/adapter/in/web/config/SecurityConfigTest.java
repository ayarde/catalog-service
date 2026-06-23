package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = {SecurityConfig.class, SecurityConfigTest.MinimalWebConfig.class})
@ActiveProfiles("test")
class SecurityConfigTest {

    @Configuration
    static class MinimalWebConfig {
        @Bean
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }

        @Bean
        org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder() {
            return mock(org.springframework.security.oauth2.jwt.JwtDecoder.class);
        }
    }

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void securityFilterChain_WhenContextLoads_ShouldBePresent() {
        assertThat(securityFilterChain).isNotNull();
    }
}
