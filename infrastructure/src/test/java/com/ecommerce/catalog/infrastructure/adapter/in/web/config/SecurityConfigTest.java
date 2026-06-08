package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {SecurityConfig.class, SecurityConfigTest.TestApp.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
class SecurityConfigTest {

    @EnableAutoConfiguration
    @org.springframework.boot.SpringBootConfiguration
    static class TestApp {
    }

    @Autowired
    private SecurityFilterChain securityFilterChain;

    /**
    En esta prueba unitaria se valida que el bean SecurityFilterChain se configura correctamente.

    Características extras:
    - Verifica la presencia del SecurityFilterChain en el contexto.
    - No requiere configuración adicional para el test.

    Se espera que el método:
    - No retorne null.
    - El bean sea una instancia válida de SecurityFilterChain.
    **/
    @Test
    void securityFilterChain_WhenContextLoads_ShouldBePresent() {
        //then
        assertThat(securityFilterChain).isNotNull();
    }
}
