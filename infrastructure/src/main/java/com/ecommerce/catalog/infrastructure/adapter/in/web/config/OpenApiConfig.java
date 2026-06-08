package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI catalogOpenAPI(@Value("${server.port:8081}") int serverPort) {
        return new OpenAPI()
                .info(new Info()
                        .title("Catalog Service API")
                        .description("API REST del catálogo de productos. "
                                + "Las operaciones de lectura (GET) son públicas; "
                                + "las de escritura requieren JWT.")
                        .version("v1"))
                .addServersItem(new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Entorno local"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT con rol ADMIN para operaciones de gestión")));
    }
}
