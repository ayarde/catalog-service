package com.ecommerce.catalog.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Disponibilidad de stock de una variante, usada por el Cart Service.
 */
@Schema(description = "Disponibilidad de stock de una variante")
public record VariantAvailabilityResponse(
        @Schema(description = "ID técnico de la variante", example = "2001")
        Long variantId,
        @Schema(description = "ID del producto padre", example = "1001")
        Long productId,
        @Schema(description = "Nombre del producto", example = "iPhone 15")
        String productName,
        @Schema(description = "Nombre de la variante", example = "Negro / 128GB")
        String variantName,
        @Schema(description = "SKU de la variante", example = "SKU-IPHONE-15-BLK-128")
        String sku,
        @Schema(description = "Indica si hay stock y el producto está disponible", example = "true")
        boolean available,
        @Schema(description = "Unidades físicas disponibles", example = "12")
        int stockQuantity,
        @Schema(description = "Estado del producto padre", example = "ACTIVE",
                allowableValues = {"DRAFT", "ACTIVE", "OUT_OF_STOCK", "ARCHIVED"})
        String productStatus
) {}
