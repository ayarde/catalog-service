package com.ecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductCommand(
        String skuBase,
        String name,
        String description,
        BigDecimal basePrice,
        String currency,
        List<String> categories,
        List<String> tags,
        java.util.Map<String, String> attributes,
        List<VariantRequest> variants,
        List<ImageRequest> images
) {}
