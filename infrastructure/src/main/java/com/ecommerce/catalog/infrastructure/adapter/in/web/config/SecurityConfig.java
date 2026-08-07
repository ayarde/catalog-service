package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;

@Configuration
@EnableWebSecurity
@Profile("!dev")
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/products/**")
                        .hasAnyAuthority("ROLE_ADMIN", "SCOPE_catalog:write")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**")
                        .hasAnyAuthority("ROLE_ADMIN", "SCOPE_catalog:write")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**")
                        .hasAnyAuthority("ROLE_ADMIN", "SCOPE_catalog:write")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**")
                        .hasAnyAuthority("ROLE_ADMIN", "SCOPE_catalog:write")
                        .anyRequest().authenticated()
                )
                .build();
    }
}
