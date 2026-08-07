package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Conversión de authorities a partir del JWT de Keycloak.
 * Combina los scopes (prefijo SCOPE_) con los roles del claim
 * anidado {@code realm_access.roles} (prefijo ROLE_), que es lo que
 * {@code hasRole("ADMIN")} requiere para las rutas de escritura.
 */
@Configuration
public class JwtAuthenticationConfig {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeAuthorities = new JwtGrantedAuthoritiesConverter();

        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter = jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>(scopeAuthorities.convert(jwt));

            Object realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
            if (realmAccess instanceof java.util.Map<?, ?> map) {
                Object roles = map.get(ROLES_CLAIM);
                if (roles instanceof Collection<?> roleList) {
                    roleList.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                            .forEach(authorities::add);
                }
            }
            return authorities;
        };

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
