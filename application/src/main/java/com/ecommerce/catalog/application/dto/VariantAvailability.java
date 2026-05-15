package com.ecommerce.catalog.application.dto;

/**
 * DTO de resultado del caso de uso CheckVariantAvailabilityUseCase.
 * Agnóstico a la capa web — no contiene anotaciones de serialización.
 *
 * @param variantId     ID técnico de la variante
 * @param productId     ID del producto padre
 * @param productName   Nombre del producto para contexto
 * @param variantName   Nombre descriptivo de la variante (ej: "Azul / 256GB")
 * @param sku           Código de stock del vendedor
 * @param available     true si stockQuantity > 0 y el producto está ACTIVE
 * @param stockQuantity Unidades disponibles
 * @param productStatus Estado actual del producto (ACTIVE, OUT_OF_STOCK, etc.)
 */
public record VariantAvailability(
        Long variantId,
        Long productId,
        String productName,
        String variantName,
        String sku,
        boolean available,
        int stockQuantity,
        String productStatus
) {}
