package com.ecommerce.catalog.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Estructura estándar de respuesta paginada.
 */
@Schema(description = "Respuesta paginada de una colección de recursos")
public record PagedResponse<T>(
    @Schema(description = "Elementos de la página actual")
    List<T> content,
    @Schema(description = "Número de página (base 0)", example = "0")
    int page,
    @Schema(description = "Tamaño de página solicitado (máximo 100)", example = "20")
    int size,
    @Schema(description = "Total de elementos en el catálogo", example = "150")
    long totalElements,
    @Schema(description = "Total de páginas disponibles", example = "8")
    int totalPages,
    @Schema(description = "Indica si es la última página", example = "false")
    boolean last
) {}
