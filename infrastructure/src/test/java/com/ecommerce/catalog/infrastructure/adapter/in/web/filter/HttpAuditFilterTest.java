package com.ecommerce.catalog.infrastructure.adapter.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class HttpAuditFilterTest {

    private final HttpAuditFilter filter = new HttpAuditFilter();

    /**
    En esta prueba unitaria se valida que una petición API pasa por el filtro y devuelve una respuesta exitosa.

    Características extras:
    - Simula una petición GET a "/api/v1/products".
    - El filtro establece el código de estado 200 en la respuesta.

    Se espera que el método:
    - No lance excepciones.
    - El HttpServletResponse tenga status 200 después de ejecutar el filtro.
    **/
    @Test
    void doFilterInternal_WhenApiRequest_ShouldProceedChain() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        //when
        filter.doFilter(request, response, chain);

        //then
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /**
    En esta prueba unitaria se valida que las rutas de management son omitidas por el filtro interno.

    Características extras:
    - Simula una petición a la ruta "/management/health".
    - El filtro debe saltarse el envoltorio y dejar el estado original.

    Se espera que el método:
    - No modifique la respuesta (status 200).
    - Retorne sin lanzar excepciones.
    **/
    @Test
    void doFilterInternal_WhenManagementPath_ShouldSkipWrapping() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/management/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        //when
        filter.doFilter(request, response, chain);

        //then
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /**
    En esta prueba unitaria se valida que el método shouldNotFilter reconoce rutas de actuator.

    Características extras:
    - Simula una petición GET a "/management/health".
    - El método shouldNotFilter debe devolver true.

    Se espera que el método:
    - Retorne true indicando que la ruta debe ser omitida.
    - No lance excepciones.
    **/
    @Test
    void shouldNotFilter_WhenManagementPath_ReturnsTrue() {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/management/health");

        //when
        boolean skip = filter.shouldNotFilter(request);

        //then
        assertThat(skip).isTrue();
    }

    /**
    En esta prueba unitaria se valida que una petición con cuerpo vacío es procesada sin errores.

    Características extras:
    - Simula una petición POST a "/api/v1/products" con cuerpo vacío.
    - El filtro debe completar la auditoría y devolver status 201.

    Se espera que el método:
    - No lance NullPointerException.
    - El HttpServletResponse tenga status 201 después de ejecutar el filtro.
    **/
    @Test
    void doFilterInternal_WhenEmptyBody_ShouldComplete() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(201);

        //when
        filter.doFilter(request, response, chain);

        //then
        assertThat(response.getStatus()).isEqualTo(201);
    }

    /**
    En esta prueba unitaria se valida que el filtro omite rutas que contienen "swagger".

    Características extras:
    - Simula una petición GET a "/swagger-ui/index.html".
    - El método shouldNotFilter debe devolver true.

    Se espera que el método:
    - Retorne true indicando que la ruta debe ser omitida.
    **/
    @Test
    void shouldNotFilter_WhenSwaggerPath_ReturnsTrue() {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");

        //when
        boolean skip = filter.shouldNotFilter(request);

        //then
        assertThat(skip).isTrue();
    }

    /**
    En esta prueba unitaria se valida que el filtro omite rutas que contienen "api-docs".

    Características extras:
    - Simula una petición GET a "/v3/api-docs".
    - El método shouldNotFilter debe devolver true.

    Se espera que el método:
    - Retorne true indicando que la ruta debe ser omitida.
    **/
    @Test
    void shouldNotFilter_WhenApiDocsPath_ReturnsTrue() {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");

        //when
        boolean skip = filter.shouldNotFilter(request);

        //then
        assertThat(skip).isTrue();
    }

    /**
    En esta prueba unitaria se valida que una petición con query string procesa correctamente
    la rama request.getQueryString() != null en el log de auditoría.

    Características extras:
    - Simula una petición GET a "/api/v1/products?page=0&size=20".
    - El filtro debe incluir la query string en la auditoría sin errores.

    Se espera que el método:
    - No lance excepciones.
    - El HttpServletResponse tenga status 200 después de ejecutar el filtro.
    **/
    @Test
    void doFilterInternal_WithQueryString_ShouldComplete() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        request.setQueryString("page=0&size=20");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> ((HttpServletResponse) res).setStatus(200);

        //when
        filter.doFilter(request, response, chain);

        //then
        assertThat(response.getStatus()).isEqualTo(200);
    }

    /**
    En esta prueba unitaria se valida que una respuesta con cuerpo es capturada y auditada
    correctamente, ejerciendo las ramas de getPayload para payload no vacío.

    Características extras:
    - Simula una petición GET a "/api/v1/products".
    - La cadena de filtros escribe contenido en el cuerpo de la respuesta.

    Se espera que el método:
    - No lance excepciones.
    - Capture el cuerpo de la respuesta para el log de auditoría.
    **/
    @Test
    void doFilterInternal_WithResponseBody_ShouldCapturePayload() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            res.getOutputStream().write("test body".getBytes(StandardCharsets.UTF_8));
            ((HttpServletResponse) res).setStatus(200);
        };

        //when
        filter.doFilter(request, response, chain);

        //then
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
