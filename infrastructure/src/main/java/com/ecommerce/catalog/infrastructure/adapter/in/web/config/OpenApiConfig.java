package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI catalogOpenAPI(
            @Value("${openapi.server.url:http://localhost:8081}") String serverUrl,
            @Value("${openapi.security.enabled:true}") boolean securityEnabled) {
        var api = new OpenAPI()
                .info(new Info()
                        .title("Catalog Service API")
                        .description("API REST del catálogo de productos. "
                                + "GET público; escritura requiere JWT con rol ADMIN.")
                        .version("v1"))
                .addServersItem(new Server()
                        .url(serverUrl)
                        .description("API Gateway"));

        if (securityEnabled) {
            api.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
               .components(new Components()
                       .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                               .type(SecurityScheme.Type.HTTP)
                               .scheme("bearer")
                               .bearerFormat("JWT")));
        }
        return api;
    }
}
