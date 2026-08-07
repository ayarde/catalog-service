package com.ecommerce.catalog.infrastructure.adapter.in.web.config;

import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {SecurityConfig.class, JwtAuthenticationConfig.class,
        SecurityAuthorizationTest.ProductsTestController.class, SecurityAuthorizationTest.TestSecurityContext.class})
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    /**
    En esta prueba unitaria se espera que el endpoint de listado de productos sea de acceso
    público cuando la petición no incluye token.

    Características extras:
    - La petición GET no incluye cabecera Authorization.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    **/
    @Test
    public void getProducts_NoToken() throws Exception {
        //given
        //Petición GET sin cabecera Authorization

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products"));

        //then
        result.andExpect(status().isOk());
    }

    /**
    En esta prueba unitaria se espera que la creación de un producto exija autenticación
    cuando la petición no incluye token.

    Características extras:
    - La petición POST no incluye cabecera Authorization.

    Se espera que el método:
    - Retorne un estado HTTP 401 (Unauthorized).
    **/
    @Test
    public void postProduct_NoToken() throws Exception {
        //given
        //Petición POST sin cabecera Authorization

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products"));

        //then
        result.andExpect(status().isUnauthorized());
    }

    /**
    En esta prueba unitaria se espera que la creación de un producto se rechace cuando el token
    autenticado no posee rol ADMIN ni scope catalog:write.

    Características extras:
    - El token contiene el scope openid y el rol user.
    - El JwtDecoder devuelve un token válido para cualquier valor de cabecera.

    Se espera que el método:
    - Retorne un estado HTTP 403 (Forbidden).
    **/
    @Test
    public void postProduct_NonAdminToken() throws Exception {
        //given
        when(jwtDecoder.decode(anyString())).thenReturn(jwt("openid", List.of("user")));

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer test-token"));

        //then
        result.andExpect(status().isForbidden());
    }

    /**
    En esta prueba unitaria se espera que la creación de un producto se permita cuando el token
    autenticado posee el rol ADMIN.

    Características extras:
    - El token contiene el scope openid y el rol ADMIN.
    - El JwtDecoder devuelve un token válido para cualquier valor de cabecera.

    Se espera que el método:
    - Retorne un estado HTTP 201 (Created).
    **/
    @Test
    public void postProduct_AdminToken() throws Exception {
        //given
        when(jwtDecoder.decode(anyString())).thenReturn(jwt("openid", List.of("ADMIN")));

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer test-token"));

        //then
        result.andExpect(status().isCreated());
    }

    /**
    En esta prueba unitaria se espera que el endpoint de listado de productos sea accesible
    para un token de máquina con el scope catalog:read.

    Características extras:
    - El token contiene el scope catalog:read.
    - El token no contiene roles realm_access.

    Se espera que el método:
    - Retorne un estado HTTP 200 (OK).
    **/
    @Test
    public void getProducts_MachineReadScope() throws Exception {
        //given
        when(jwtDecoder.decode(anyString())).thenReturn(jwt("catalog:read", null));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products").header("Authorization", "Bearer test-token"));

        //then
        result.andExpect(status().isOk());
    }

    /**
    En esta prueba unitaria se espera que la creación de un producto se permita para un token
    de máquina que posee los scopes catalog:read y catalog:write.

    Características extras:
    - El token contiene los scopes catalog:read y catalog:write.
    - El token no contiene roles realm_access.

    Se espera que el método:
    - Retorne un estado HTTP 201 (Created).
    **/
    @Test
    public void postProduct_MachineWriteScope() throws Exception {
        //given
        when(jwtDecoder.decode(anyString())).thenReturn(jwt("catalog:read catalog:write", null));

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer test-token"));

        //then
        result.andExpect(status().isCreated());
    }

    /**
    En esta prueba unitaria se espera que la creación de un producto se rechace para un token
    de máquina que únicamente posee el scope catalog:read.

    Características extras:
    - El token contiene únicamente el scope catalog:read.
    - El token no contiene roles realm_access.

    Se espera que el método:
    - Retorne un estado HTTP 403 (Forbidden).
    **/
    @Test
    public void postProduct_MachineReadOnlyScope() throws Exception {
        //given
        when(jwtDecoder.decode(anyString())).thenReturn(jwt("catalog:read", null));

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products").header("Authorization", "Bearer test-token"));

        //then
        result.andExpect(status().isForbidden());
    }

    @Configuration
    static class TestSecurityContext {
        @Bean
        HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
            return new HandlerMappingIntrospector();
        }
    }

    @RestController
    static class ProductsTestController {
        @GetMapping("/api/v1/products")
        ResponseEntity<String> list() {
            return ResponseEntity.ok("ok");
        }

        @PostMapping("/api/v1/products")
        ResponseEntity<String> create() {
            return ResponseEntity.status(HttpStatus.CREATED).body("created");
        }
    }

    //<editor-fold desc="Métodos auxiliares">
    private Jwt jwt(String scope, List<String> realmRoles) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-1");
        if (scope != null) {
            builder.claim("scope", scope);
        }
        if (realmRoles != null) {
            builder.claim("realm_access", Map.of("roles", realmRoles));
        }
        return builder.build();
    }
    //</editor-fold>
}
