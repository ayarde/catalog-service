package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceServerJwtConfigTest {

    private final ResourceServerJwtConfig config = new ResourceServerJwtConfig();

    /**
    En esta prueba unitaria se espera que el validador de audiencia acepte un token
    cuya claim aud contenga catalog-service dentro de una colección.

    Características extras:
    - La claim aud es una lista que incluye catalog-service.
    - El validador se configura con la audiencia esperada catalog-service.

    Se espera que el método:
    - Valide correctamente el token.
    - No reporte errores.
    **/
    @Test
    public void validate_AudienceContainsCatalogService() {
        //given
        Jwt jwt = jwtWithAudience(List.of("account", "catalog-service"));

        //when
        OAuth2TokenValidatorResult result = config.audienceValidator("catalog-service").validate(jwt);

        //then
        assertThat(result.hasErrors())
                .as("El token debe ser válido cuando la audiencia contiene catalog-service")
                .isFalse();
    }

    /**
    En esta prueba unitaria se espera que el validador de audiencia acepte un token
    cuya claim aud sea un String que coincide exactamente con catalog-service.

    Características extras:
    - La claim aud es un String único igual a catalog-service.

    Se espera que el método:
    - Valide correctamente el token.
    - No reporte errores.
    **/
    @Test
    public void validate_SingleStringAudienceMatches() {
        //given
        Jwt jwt = jwtWithAudience("catalog-service");

        //when
        OAuth2TokenValidatorResult result = config.audienceValidator("catalog-service").validate(jwt);

        //then
        assertThat(result.hasErrors())
                .as("El token debe ser válido cuando la audiencia coincide exactamente")
                .isFalse();
    }

    /**
    En esta prueba unitaria se espera que el validador de audiencia rechace un token
    cuya claim aud no contenga catalog-service.

    Características extras:
    - La claim aud es una lista que no incluye catalog-service.

    Se espera que el método:
    - Reporte errores de validación.
    **/
    @Test
    public void validate_AudienceWithoutCatalogService() {
        //given
        Jwt jwt = jwtWithAudience(List.of("account"));

        //when
        OAuth2TokenValidatorResult result = config.audienceValidator("catalog-service").validate(jwt);

        //then
        assertThat(result.hasErrors())
                .as("El token debe ser inválido cuando la audiencia no contiene catalog-service")
                .isTrue();
    }

    /**
    En esta prueba unitaria se espera que el validador de audiencia rechace un token
    que no posee la claim aud.

    Características extras:
    - El token no incluye la claim aud.

    Se espera que el método:
    - Reporte errores de validación.
    **/
    @Test
    public void validate_AudienceMissing() {
        //given
        Jwt jwt = jwtWithAudience(null);

        //when
        OAuth2TokenValidatorResult result = config.audienceValidator("catalog-service").validate(jwt);

        //then
        assertThat(result.hasErrors())
                .as("El token debe ser inválido cuando falta la audiencia")
                .isTrue();
    }

    /**
    En esta prueba unitaria se espera que el validador de audiencia rechace un token
    cuya claim aud sea de un tipo no soportado.

    Características extras:
    - La claim aud es un Integer en lugar de String o colección.

    Se espera que el método:
    - Reporte errores de validación.
    **/
    @Test
    public void validate_AudienceUnsupportedType() {
        //given
        Jwt jwt = jwtWithAudience(12345);

        //when
        OAuth2TokenValidatorResult result = config.audienceValidator("catalog-service").validate(jwt);

        //then
        assertThat(result.hasErrors())
                .as("El token debe ser inválido cuando la audiencia tiene un tipo no soportado")
                .isTrue();
    }

    //<editor-fold desc="Métodos auxiliares">
    private Jwt jwtWithAudience(Object aud) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "machine");
        if (aud != null) {
            builder.claim("aud", aud);
        }
        return builder.build();
    }
    //</editor-fold>
}
