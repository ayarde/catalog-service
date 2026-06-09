package com.ecommerce.catalog.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Clase principal de arranque del microservicio Catalog.
 * Escaneamos los componentes y repositorios definidos en el módulo de infraestructura.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.ecommerce.catalog")
@EnableMongoRepositories(basePackages = "com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.repository")
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
