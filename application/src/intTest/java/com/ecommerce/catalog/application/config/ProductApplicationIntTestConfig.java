package com.ecommerce.catalog.application.config;

import com.ecommerce.catalog.application.service.ProductService;
import com.ecommerce.catalog.domain.port.out.EventPublisher;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import com.ecommerce.catalog.domain.port.util.SlugGenerator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

/**
 * Configuración compartida para los tests de integración del catálogo.
 * Provee mocks para los puertos de salida y activa la infraestructura de caché.
 */
@Configuration
@EnableCaching
public class ProductApplicationIntTestConfig {

    @Bean
    public ProductRepository productRepository() {
        return mock(ProductRepository.class);
    }

    @Bean
    public EventPublisher eventPublisher() {
        return mock(EventPublisher.class);
    }

    @Bean
    public SlugGenerator slugGenerator() {
        return mock(SlugGenerator.class);
    }

    @Bean
    public ProductService productService(ProductRepository productRepository, 
                                       EventPublisher eventPublisher, 
                                       SlugGenerator slugGenerator) {
        return new ProductService(productRepository, eventPublisher, slugGenerator);
    }

    @Bean
    public ConcurrentMapCacheManager cacheManager() {
        return new ConcurrentMapCacheManager("product", "products_list", "product_slug");
    }
}
