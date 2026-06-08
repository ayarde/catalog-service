package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import com.ecommerce.catalog.infrastructure.adapter.in.web.filter.HttpAuditFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<HttpAuditFilter> auditFilterRegistration(HttpAuditFilter auditFilter) {
        FilterRegistrationBean<HttpAuditFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(auditFilter);
        registrationBean.addUrlPatterns("/api/*"); // Solo auditar rutas de API
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE - 10); // Ejecutar después de seguridad y trazabilidad
        return registrationBean;
    }
}
