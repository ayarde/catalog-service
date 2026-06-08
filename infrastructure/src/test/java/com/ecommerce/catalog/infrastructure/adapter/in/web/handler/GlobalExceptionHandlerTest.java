package com.ecommerce.catalog.infrastructure.adapter.in.web.handler;

import com.ecommerce.catalog.domain.exception.AlreadyExistsException;
import com.ecommerce.catalog.domain.exception.NotFoundException;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ValidationErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private Tracer tracer;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(messageSource, tracer);
    }

    /**
    En esta prueba unitaria se espera que se valide el manejo de conflicto por producto duplicado.

    Características extras:
    - Simula una excepción de producto duplicado (AlreadyExistsException).

    Se espera que el método:
    - Retorne un ResponseEntity con código 409 (Conflict).
    - Devuelva el código de error correspondiente en el cuerpo de la respuesta.
    **/
    @Test
    void handleAlreadyExists_WhenThrown_ShouldReturn409() {
        //given
        AlreadyExistsException ex = new AlreadyExistsException("error.product.already_exists", "SKU");
        when(messageSource.getMessage(eq("error.product.already_exists"), any(), any(Locale.class)))
                .thenReturn("Ya existe");

        //when
        ResponseEntity<ErrorResponse> response = handler.handleAlreadyExists(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().code()).isEqualTo("PRODUCT_ALREADY_EXISTS");
    }

    /**
    En esta prueba unitaria se espera que se valide el manejo de recursos no encontrados en el sistema.

    Características extras:
    - Simula una excepción de recurso no encontrado (NotFoundException).

    Se espera que el método:
    - Retorne un ResponseEntity con código 404 (Not Found).
    **/
    @Test
    void handleNotFound_WhenThrown_ShouldReturn404() {
        //given
        NotFoundException ex = new NotFoundException("error.product.not_found", 1L);
        when(messageSource.getMessage(eq("error.product.not_found"), any(), any(Locale.class)))
                .thenReturn("No encontrado");

        //when
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
    En esta prueba unitaria se espera que se valide el manejo de errores de validación de argumentos.

    Características extras:
    - Simula un MethodArgumentNotValidException con errores en campos de entrada.

    Se espera que el método:
    - Retorne un ResponseEntity con código 400 (Bad Request).
    - Devuelva un mapa con los nombres de los campos erróneos y sus descripciones.
    **/
    @Test
    void handleValidationExceptions_WhenInvalidInput_ShouldReturn400() {
        //given
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "name", "required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        //when
        ResponseEntity<ValidationErrorResponse> response = handler.handleValidationExceptions(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("name");
    }

    /**
    En esta prueba unitaria se espera que se valide el manejo de excepciones por límite de tasa excedido.

    Características extras:
    - Simula una excepción RequestNotPermitted de Resilience4j.

    Se espera que el método:
    - Retorne un ResponseEntity con código 429 (Too Many Requests).
    **/
    @Test
    void handleRateLimit_WhenTriggered_ShouldReturn429() {
        //given
        when(messageSource.getMessage(eq("error.resilience.rate_limit"), isNull(), any(Locale.class)))
                .thenReturn("Rate limit");

        //when
        RequestNotPermitted rateLimitEx = org.mockito.Mockito.mock(RequestNotPermitted.class);
        ResponseEntity<ErrorResponse> response =
                handler.handleRateLimit(rateLimitEx);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
    En esta prueba unitaria se espera que se valide el manejo de excepciones cuando el Circuit Breaker está abierto.

    Características extras:
    - Simula una excepción CallNotPermittedException cuando el circuito está abierto.

    Se espera que el método:
    - Retorne un ResponseEntity con código 503 (Service Unavailable).
    **/
    @Test
    void handleCircuitBreaker_WhenOpen_ShouldReturn503() {
        //given
        when(messageSource.getMessage(eq("error.resilience.circuit_breaker"), isNull(), any(Locale.class)))
                .thenReturn("Circuit open");

        //when
        ResponseEntity<ErrorResponse> response =
                handler.handleCircuitBreaker(CallNotPermittedException.createCallNotPermittedException(
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults("test")));

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
    En esta prueba unitaria se espera que se valide el manejo de peticiones a rutas inexistentes (recursos estáticos/físicos).

    Características extras:
    - Simula una excepción NoResourceFoundException de Spring.

    Se espera que el método:
    - Retorne un ResponseEntity con código 404 (Not Found).
    **/
    @Test
    void handleNoResourceFound_WhenUnknownPath_ShouldReturn404() {
        //given
        NoResourceFoundException ex = new NoResourceFoundException(
                org.springframework.http.HttpMethod.GET, "/missing");

        //when
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
    En esta prueba unitaria se espera que se valide el manejo de excepciones generales no controladas.

    Características extras:
    - Simula una RuntimeException inesperada.

    Se espera que el método:
    - Retorne un ResponseEntity con código 500 (Internal Server Error).
    **/
    @Test
    void handleGeneralException_WhenUnexpected_ShouldReturn500() {
        //when
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneralException(new RuntimeException("boom"));

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
