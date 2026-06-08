package com.ecommerce.catalog.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;

/**
 * Mapa de errores de validación indexado por nombre de campo.
 */
@Schema(
        description = "Errores de validación por campo",
        example = "{\"skuBase\": \"El SKU base es obligatorio\", \"name\": \"El nombre es obligatorio\"}"
)
public class ValidationErrorResponse extends HashMap<String, String> {}
