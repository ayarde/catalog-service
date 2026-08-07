package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = {SecurityConfig.class, JwtAuthenticationConfig.class, SecurityConfigTest.MinimalWebConfig.class})
@ActiveProfiles("test")
class SecurityConfigTest {

    @Configuration
    static class MinimalWebConfig {
        @Bean
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter jwtAuthenticationConverter;

    /**
    En esta prueba unitaria se espera que el bean SecurityFilterChain se registre en el contexto Spring.

    Características extras:
    - El contexto carga las clases de configuración de seguridad y JWT.
    - Se proporciona un JwtDecoder mockeado como dependencia.

    Se espera que el método:
    - Exponga el bean SecurityFilterChain en el contexto.
    - El bean no sea nulo.
    **/
    @Test
    public void securityFilterChain_LoadsInContext() {
        //given
        //Contexto Spring cargado con las clases de configuración de seguridad

        //when
        SecurityFilterChain chain = securityFilterChain;

        //then
        assertThat(chain).as("La cadena de filtros de seguridad debe estar disponible").isNotNull();
    }

    /**
    En esta prueba unitaria se espera que el bean JwtAuthenticationConverter se registre en el contexto Spring.

    Características extras:
    - El contexto carga la configuración de autenticación JWT.
    - Se proporciona un JwtDecoder mockeado como dependencia.

    Se espera que el método:
    - Exponga el bean JwtAuthenticationConverter en el contexto.
    - El bean no sea nulo.
    **/
    @Test
    public void jwtAuthenticationConverter_LoadsInContext() {
        //given
        //Contexto Spring cargado con la configuración de autenticación JWT

        //when
        org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter converter = jwtAuthenticationConverter;

        //then
        assertThat(converter).as("El conversor de autenticación JWT debe estar disponible").isNotNull();
    }
}
