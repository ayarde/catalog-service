package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import com.ecommerce.catalog.infrastructure.adapter.in.web.filter.HttpAuditFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {FilterConfig.class, HttpAuditFilter.class})
@ActiveProfiles("test")
class FilterConfigTest {

    @Autowired
    private FilterRegistrationBean<HttpAuditFilter> auditFilterRegistration;

    /**
    En esta prueba unitaria se espera que el filtro de auditoría HTTP se registre en el contexto
    Spring y que se aplique a las rutas de la API.

    Características extras:
    - El contexto carga la configuración de filtros y el filtro HttpAuditFilter.
    - El registro debe exponer el filtro y sus patrones de URL.

    Se espera que el método:
    - Exponga un filtro no nulo en el registro.
    - Registre el patrón de URL /api/*.
    **/
    @Test
    public void auditFilterRegistration_RegistersFilterForApiPath() {
        //given
        //Contexto Spring cargado con la configuración de filtros

        //when
        FilterRegistrationBean<HttpAuditFilter> registration = auditFilterRegistration;

        //then
        assertThat(registration.getFilter()).as("El filtro de auditoría debe estar registrado").isNotNull();
        assertThat(registration.getUrlPatterns()).as("El filtro debe aplicarse a /api/*").contains("/api/*");
    }
}
