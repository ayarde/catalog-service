package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import com.ecommerce.catalog.infrastructure.adapter.in.web.filter.HttpAuditFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {FilterConfig.class, HttpAuditFilter.class})
class FilterConfigTest {

    @Autowired
    private FilterRegistrationBean<HttpAuditFilter> auditFilterRegistration;

    /**
    En esta prueba unitaria se valida el registro del filtro de auditoría HTTP.

    Características extras:
    - Verifica que el filtro esté registrado en el contexto de Spring.
    - Se asegura que el URL pattern sea "/api/*".

    Se espera que el método:
    - No retorne null.
    - El FilterRegistrationBean contenga una instancia de HttpAuditFilter.
    */
    @Test
    void auditFilterRegistration_WhenContextLoads_ShouldRegisterFilter() {
        //then
        assertThat(auditFilterRegistration.getFilter()).isNotNull();
        assertThat(auditFilterRegistration.getUrlPatterns()).contains("/api/*");
    }
}
