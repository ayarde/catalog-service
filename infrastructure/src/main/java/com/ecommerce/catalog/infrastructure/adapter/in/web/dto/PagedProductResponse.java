package com.ecommerce.catalog.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Tipo concreto para documentar en OpenAPI la respuesta paginada de productos.
 */
@Schema(description = "Listado paginado de productos")
public record PagedProductResponse(
        @Schema(description = "Productos de la página actual")
        List<ProductResponse> content,
        @Schema(description = "Número de página (base 0)", example = "0")
        int page,
        @Schema(description = "Tamaño de página (máximo 100)", example = "20")
        int size,
        @Schema(description = "Total de elementos", example = "150")
        long totalElements,
        @Schema(description = "Total de páginas", example = "8")
        int totalPages,
        @Schema(description = "Indica si es la última página", example = "false")
        boolean last
) {}
