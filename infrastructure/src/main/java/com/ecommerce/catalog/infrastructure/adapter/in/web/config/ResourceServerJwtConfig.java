package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Collection;

/**
 * Configuración del {@link JwtDecoder} del resource server.
 * Valida firma, timestamp e issuer (vía Keycloak), y además el claim
 * {@code aud}, que debe contener la audiencia {@code catalog-service}
 * (tolerando tanto String como List&lt;String&gt;). Keycloak emite
 * normalmente {@code aud=["account", ...]}, por lo que el mapper de
 * audiencia del client scope añade {@code catalog-service} a esa lista.
 */
@Configuration
@Profile("!dev")
public class ResourceServerJwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${app.security.audience}") String audience) {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuerUri);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuerUri),
                audienceValidator(audience)));
        return decoder;
    }

    OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        return new JwtClaimValidator<>("aud", claim -> {
            if (claim instanceof String single) {
                return single.equals(audience);
            }
            if (claim instanceof Collection<?> list) {
                return list.contains(audience);
            }
            return false;
        });
    }
}
