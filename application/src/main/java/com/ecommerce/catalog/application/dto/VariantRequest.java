package com.ecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.Map;

public record VariantRequest(
        String sku,
        String variantName,
        BigDecimal price,
        String currency,
        Integer stockQuantity,
        Map<String, String> attributes
) {}
