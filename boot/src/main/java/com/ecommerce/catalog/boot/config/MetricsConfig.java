package com.ecommerce.catalog.boot.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    MeterRegistryCustomizer<PrometheusMeterRegistry> commonTags() {
        return registry -> registry.config().commonTags("application", "catalog-service");
    }

    @Bean
    MeterBinder circuitBreakerMetrics(CircuitBreakerRegistry registry) {
        return meterRegistry ->
                TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meterRegistry);
    }

    @Bean
    MeterBinder rateLimiterMetrics(RateLimiterRegistry registry) {
        return meterRegistry ->
                TaggedRateLimiterMetrics.ofRateLimiterRegistry(registry).bindTo(meterRegistry);
    }
}
