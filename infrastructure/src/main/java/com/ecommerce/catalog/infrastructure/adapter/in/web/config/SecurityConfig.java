package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Deshabilitamos CSRF ya que somos una API Stateless
            .csrf(csrf -> csrf.disable())
            
            // Política de sesión sin estado (JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Reglas de Autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints Públicos (Catálogo de Lectura)
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                
                // Endpoints de Actuator (Observabilidad) - Dinámico
                .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                
                // Endpoints de Documentación (Swagger/OpenAPI)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Endpoints Protegidos (Gestión)
                // En producción, aquí se validaría el rol 'ADMIN' del JWT
                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .build();
    }
}
