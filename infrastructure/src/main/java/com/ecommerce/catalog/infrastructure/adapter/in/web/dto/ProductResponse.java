package com.ecommerce.catalog.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Representación del producto enviada al cliente.
 */
@Schema(description = "Producto del catálogo")
public record ProductResponse(
    @Schema(description = "Identificador único del producto", example = "1001")
    Long id,
    @Schema(description = "SKU base del producto", example = "SKU-IPHONE-15")
    String skuBase,
    @Schema(description = "Nombre comercial", example = "iPhone 15")
    String name,
    @Schema(description = "URL amigable para SEO", example = "iphone-15")
    String slug,
    @Schema(description = "Descripción del producto")
    String description,
    @Schema(description = "Precio base", example = "999.99")
    BigDecimal basePrice,
    @Schema(description = "Moneda ISO", example = "EUR")
    String currency,
    @Schema(description = "Categorías asignadas")
    List<String> categories,
    @Schema(description = "Etiquetas de búsqueda")
    List<String> tags,
    @Schema(description = "Atributos personalizados")
    Map<String, String> attributes,
    @Schema(description = "Estado del producto", example = "DRAFT",
            allowableValues = {"DRAFT", "ACTIVE", "OUT_OF_STOCK", "ARCHIVED"})
    String status,
    @Schema(description = "Variantes disponibles")
    List<VariantResponse> variants,
    @Schema(description = "Imágenes del producto")
    List<ImageResponse> images,
    @Schema(description = "Fecha de creación", example = "2026-06-01T10:00:00Z")
    Instant createdAt,
    @Schema(description = "Fecha de última actualización", example = "2026-06-01T12:00:00Z")
    Instant updatedAt
) {
    @Schema(description = "Variante de producto")
    public record VariantResponse(
        @Schema(description = "ID de la variante", example = "2001")
        Long variantId,
        @Schema(description = "SKU de la variante", example = "SKU-IPHONE-15-BLK-128")
        String sku,
        @Schema(description = "Nombre de la variante", example = "Negro / 128GB")
        String variantName,
        @Schema(description = "Precio de la variante", example = "999.99")
        BigDecimal price,
        @Schema(description = "Moneda ISO", example = "EUR")
        String currency,
        @Schema(description = "Unidades en stock", example = "50")
        Integer stockQuantity,
        @Schema(description = "Atributos de la variante")
        Map<String, String> attributes
    ) {}

    @Schema(description = "Imagen del producto")
    public record ImageResponse(
        @Schema(description = "URL de la imagen", example = "https://cdn.example.com/img1.jpg")
        String url,
        @Schema(description = "Texto alternativo")
        String altText,
        @Schema(description = "Orden de visualización", example = "0")
        Integer sortOrder
    ) {}
}
