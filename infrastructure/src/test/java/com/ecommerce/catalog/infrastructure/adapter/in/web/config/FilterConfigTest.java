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

    @Test
    void auditFilterRegistration_WhenContextLoads_ShouldRegisterFilter() {
        assertThat(auditFilterRegistration.getFilter()).isNotNull();
        assertThat(auditFilterRegistration.getUrlPatterns()).contains("/api/*");
    }
}
