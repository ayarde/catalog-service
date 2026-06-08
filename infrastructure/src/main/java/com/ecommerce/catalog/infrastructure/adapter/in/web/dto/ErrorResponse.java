package com.ecommerce.catalog.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Estructura estandarizada para respuestas de error de la API.
 * El campo {@code traceId} permite correlacionar el error con los logs del servidor.
 */
@Schema(description = "Respuesta de error estandarizada de la API")
public record ErrorResponse(
        @Schema(description = "Código HTTP", example = "404")
        int status,
        @Schema(description = "Código de error de negocio", example = "PRODUCT_NOT_FOUND")
        String code,
        @Schema(description = "Mensaje legible para el cliente", example = "Producto no encontrado")
        String message,
        @Schema(description = "Identificador de traza para soporte", example = "abc123def456")
        String traceId,
        @Schema(description = "Marca temporal del error", example = "2026-06-01T12:00:00Z")
        Instant timestamp
) {}
