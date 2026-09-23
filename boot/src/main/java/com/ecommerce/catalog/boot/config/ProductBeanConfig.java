package com.ecommerce.catalog.boot.config;

import com.ecommerce.catalog.application.service.ProductService;
import com.ecommerce.catalog.application.port.out.EventPublisher;
import com.ecommerce.catalog.application.port.out.ProductRepository;
import com.ecommerce.catalog.application.port.util.SlugGenerator;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración centralizada de Beans para la arquitectura hexagonal.
 * Aquí instanciamos manualmente los servicios de la capa de Aplicación
 * inyectando sus dependencias de Infraestructura.
 */
@Configuration
public class ProductBeanConfig {

    @Bean
    public ProductService productService(ProductRepository repository, EventPublisher eventPublisher,
                                         SlugGenerator slugGenerator, MeterRegistry meterRegistry) {
        return new ProductService(repository, eventPublisher, slugGenerator, meterRegistry);
    }
}
