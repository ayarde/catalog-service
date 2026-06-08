package com.ecommerce.catalog.infrastructure.adapter.in.web.handler;

import com.ecommerce.catalog.domain.exception.AlreadyExistsException;
import com.ecommerce.catalog.domain.exception.NotFoundException;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.ecommerce.catalog.infrastructure.adapter.in.web.dto.ValidationErrorResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

/**
 * Interceptor global de excepciones con soporte para Internacionalización (i18n).
 * Traduce los códigos de error del dominio a mensajes legibles según el Locale del usuario.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;
    private final Tracer tracer;

    public GlobalExceptionHandler(MessageSource messageSource, Tracer tracer) {
        this.messageSource = messageSource;
        this.tracer = tracer;
    }

    /**
     * Captura intentos de crear un producto que ya existe.
     * Usa el MessageSource para traducir la llave recibida desde el dominio.
     */
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(AlreadyExistsException ex) {
        String localizedMessage = messageSource.getMessage(
                ex.getMessage(), 
                ex.getArgs(), 
                LocaleContextHolder.getLocale()
        );

        ErrorResponse error = createErrorResponse(
                HttpStatus.CONFLICT,
                "PRODUCT_ALREADY_EXISTS",
                localizedMessage
        );
        log.warn("Business Conflict: {}", localizedMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Captura búsquedas o modificaciones de productos inexistentes.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        String localizedMessage = messageSource.getMessage(
                ex.getMessage(), 
                ex.getArgs(), 
                LocaleContextHolder.getLocale()
        );

        ErrorResponse error = createErrorResponse(
                HttpStatus.NOT_FOUND,
                "PRODUCT_NOT_FOUND",
                localizedMessage
        );
        log.warn("Resource Not Found: {}", localizedMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Captura fallos de validación del JSON de entrada (@Valid).
     * Retorna HTTP 400 Bad Request con los detalles traducidos si estuvieran configurados.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ValidationErrorResponse errors = new ValidationErrorResponse();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {} errors found", errors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Captura cuando el Rate Limiter bloquea peticiones (Too Many Requests).
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RequestNotPermitted ex) {
        String localizedMessage = messageSource.getMessage(
                "error.resilience.rate_limit", null, LocaleContextHolder.getLocale());
        
        ErrorResponse error = createErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "RATE_LIMIT_EXCEEDED",
                localizedMessage
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    /**
     * Captura cuando el Circuit Breaker está abierto (Service Unavailable).
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreaker(CallNotPermittedException ex) {
        String localizedMessage = messageSource.getMessage(
                "error.resilience.circuit_breaker", null, LocaleContextHolder.getLocale());
        
        ErrorResponse error = createErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "CIRCUIT_BREAKER_OPEN",
                localizedMessage
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Captura rutas que no existen (404) para evitar que se reporten como fallos del servidor (500).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        ErrorResponse error = createErrorResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                "The requested resource was not found: " + ex.getResourcePath()
        );
        log.warn("Resource Not Found (404): {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Captura cualquier otra excepción no controlada.
     * Retorna HTTP 500 Internal Server Error y loguea el stack trace completo.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        
        ErrorResponse error = createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Crea una respuesta de error estandarizada incluyendo el traceId activo.
     */
    private ErrorResponse createErrorResponse(HttpStatus status, String code, String message) {
        String traceId = (tracer.currentSpan() != null) 
                ? tracer.currentSpan().context().traceId() 
                : "N/A";
                
        return new ErrorResponse(
                status.value(),
                code,
                message,
                traceId,
                Instant.now()
        );
    }

}
