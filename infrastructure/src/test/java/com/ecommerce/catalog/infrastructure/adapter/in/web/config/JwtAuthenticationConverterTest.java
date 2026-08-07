package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConfig config = new JwtAuthenticationConfig();

    /**
    En esta prueba unitaria se espera que el conversor JWT exponga las autoridades de rol
    y de scope cuando el token contiene realm_access.roles y scope.

    Características extras:
    - El token contiene el rol ADMIN y el rol offline_access en realm_access.
    - El token contiene los scopes openid y profile.

    Se espera que el método:
    - Convierta los scopes en autoridades SCOPE_openid y SCOPE_profile.
    - Convierta los roles en autoridades ROLE_ADMIN y ROLE_offline_access.
    **/
    @Test
    public void convert_TokenWithRealmRoles() {
        //given
        Jwt jwt = jwtWithClaims(Map.of(
                "scope", "openid profile",
                "realm_access", Map.of("roles", List.of("ADMIN", "offline_access"))
        ));

        //when
        AbstractAuthenticationToken auth = config.jwtAuthenticationConverter().convert(jwt);

        //then
        assertThat(auth.getAuthorities())
                .as("Las autoridades deben incluir scopes y roles del token")
                .extracting("authority")
                .contains("SCOPE_openid", "SCOPE_profile", "ROLE_ADMIN", "ROLE_offline_access");
    }

    /**
    En esta prueba unitaria se espera que el conversor JWT exponga únicamente las autoridades
    de scope cuando el token solo contiene la claim scope.

    Características extras:
    - El token contiene el scope openid.
    - El token no contiene realm_access.

    Se espera que el método:
    - Convierta el scope en la autoridad SCOPE_openid.
    - No genere autoridades de rol.
    **/
    @Test
    public void convert_TokenWithOnlyScopes() {
        //given
        Jwt jwt = jwtWithClaims(Map.of("scope", "openid"));

        //when
        AbstractAuthenticationToken auth = config.jwtAuthenticationConverter().convert(jwt);

        //then
        assertThat(auth.getAuthorities())
                .as("Las autoridades deben contener únicamente el scope openid")
                .extracting("authority")
                .containsExactlyInAnyOrder("SCOPE_openid");
    }

    /**
    En esta prueba unitaria se espera que el conversor JWT ignore la ausencia de roles
    y exponga únicamente las autoridades de scope.

    Características extras:
    - El token contiene el scope read.
    - El token no contiene realm_access.

    Se espera que el método:
    - Convierta el scope en la autoridad SCOPE_read.
    - No genere autoridades de rol.
    **/
    @Test
    public void convert_TokenWithoutRealmRoles() {
        //given
        Jwt jwt = jwtWithClaims(Map.of("scope", "read"));

        //when
        AbstractAuthenticationToken auth = config.jwtAuthenticationConverter().convert(jwt);

        //then
        assertThat(auth.getAuthorities())
                .as("Las autoridades deben contener únicamente el scope read")
                .extracting("authority")
                .containsExactlyInAnyOrder("SCOPE_read");
    }

    /**
    En esta prueba unitaria se espera que el conversor JWT exponga las autoridades de scope
    de catálogo cuando el token contiene los scopes catalog:read y catalog:write.

    Características extras:
    - El token contiene los scopes catalog:read y catalog:write.
    - El token no contiene realm_access.

    Se espera que el método:
    - Convierta los scopes en autoridades SCOPE_catalog:read y SCOPE_catalog:write.
    **/
    @Test
    public void convert_TokenWithCatalogScopes() {
        //given
        Jwt jwt = jwtWithClaims(Map.of("scope", "catalog:read catalog:write"));

        //when
        AbstractAuthenticationToken auth = config.jwtAuthenticationConverter().convert(jwt);

        //then
        assertThat(auth.getAuthorities())
                .as("Las autoridades deben incluir los scopes de catálogo")
                .extracting("authority")
                .containsExactlyInAnyOrder("SCOPE_catalog:read", "SCOPE_catalog:write");
    }

    /**
    En esta prueba unitaria se espera que el conversor JWT no exponga ninguna autoridad
    cuando el token no contiene claims de scope ni de roles.

    Características extras:
    - El token únicamente contiene la claim sub.

    Se espera que el método:
    - Retorne un token de autenticación sin autoridades.
    **/
    @Test
    public void convert_TokenWithoutRelevantClaims() {
        //given
        Jwt jwt = jwtWithClaims(Map.of("sub", "user-1"));

        //when
        AbstractAuthenticationToken auth = config.jwtAuthenticationConverter().convert(jwt);

        //then
        assertThat(auth.getAuthorities())
                .as("Las autoridades deben estar vacías")
                .isEmpty();
    }

    /**
    En esta prueba unitaria se espera que el conversor JWT no falle cuando realm_access
    no contiene la claim roles.

    Características extras:
    - El token contiene realm_access con una clave no relacionada con roles.

    Se espera que el método:
    - No lance ninguna excepción.
    - Retorne un token de autenticación sin autoridades.
    **/
    @Test
    public void convert_RealmAccessWithoutRolesClaim() {
        //given
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("other", "value")));

        //when
        AbstractAuthenticationToken auth = config.jwtAuthenticationConverter().convert(jwt);

        //then
        assertThat(auth.getAuthorities())
                .as("Las autoridades deben estar vacías")
                .isEmpty();
    }

    //<editor-fold desc="Métodos auxiliares">
    private Jwt jwtWithClaims(Map<String, Object> claims) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .build();
    }
    //</editor-fold>
}
