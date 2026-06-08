package com.ecommerce.catalog.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Payload de entrada para la creación o actualización de un producto.
 */
@Schema(description = "Datos para crear o actualizar un producto en el catálogo")
public record ProductCreateRequest(
    @Schema(description = "SKU único del producto", example = "SKU-IPHONE-15", requiredMode = REQUIRED)
    @NotBlank(message = "El SKU base es obligatorio")
    String skuBase,

    @Schema(description = "Nombre comercial del producto", example = "iPhone 15", requiredMode = REQUIRED)
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @Schema(description = "Descripción detallada del producto", example = "Smartphone Apple 128GB")
    String description,

    @Schema(description = "Precio base del producto", example = "999.99", requiredMode = REQUIRED)
    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio base no puede ser negativo")
    BigDecimal basePrice,

    @Schema(description = "Código ISO de moneda", example = "EUR", requiredMode = REQUIRED)
    @NotBlank(message = "La moneda es obligatoria")
    String currency,

    @Schema(description = "Categorías asignadas al producto", example = "[\"electronics\", \"smartphones\"]")
    List<String> categories,

    @Schema(description = "Etiquetas de búsqueda", example = "[\"apple\", \"5g\"]")
    List<String> tags,

    @Schema(description = "Atributos personalizados clave-valor", example = "{\"color\": \"negro\"}")
    Map<String, String> attributes,

    @Schema(description = "Variantes del producto (talla, color, etc.)")
    @Valid
    List<VariantRequest> variants,

    @Schema(description = "Imágenes asociadas al producto")
    @Valid
    List<ImageRequest> images
) {
    @Schema(description = "Variante de un producto con precio y stock propios")
    public record VariantRequest(
        @Schema(description = "SKU de la variante", example = "SKU-IPHONE-15-BLK-128", requiredMode = REQUIRED)
        @NotBlank(message = "El SKU de la variante es obligatorio")
        String sku,

        @Schema(description = "Nombre descriptivo de la variante", example = "Negro / 128GB", requiredMode = REQUIRED)
        @NotBlank(message = "El nombre de la variante es obligatorio")
        String variantName,

        @Schema(description = "Precio de la variante", example = "999.99", requiredMode = REQUIRED)
        @NotNull(message = "El precio de la variante es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        BigDecimal price,

        @Schema(description = "Código ISO de moneda", example = "EUR", requiredMode = REQUIRED)
        @NotBlank(message = "La moneda es obligatoria")
        String currency,

        @Schema(description = "Unidades en stock", example = "50", requiredMode = NOT_REQUIRED)
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stockQuantity,

        @Schema(description = "Atributos específicos de la variante", example = "{\"storage\": \"128GB\"}")
        Map<String, String> attributes
    ) {}

    @Schema(description = "Imagen del producto")
    public record ImageRequest(
        @Schema(description = "URL pública de la imagen", example = "https://cdn.example.com/img1.jpg", requiredMode = REQUIRED)
        @NotBlank(message = "La URL de la imagen es obligatoria")
        String url,

        @Schema(description = "Texto alternativo para accesibilidad", example = "iPhone 15 vista frontal")
        String altText,

        @Schema(description = "Orden de visualización (0 = primera)", example = "0", requiredMode = REQUIRED)
        @NotNull(message = "El orden de la imagen es obligatorio")
        @Min(value = 0, message = "El orden no puede ser negativo")
        Integer sortOrder
    ) {}
}
