package com.ecommerce.catalog.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductCommand(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        String currency,
        List<VariantRequest> variants,
        List<ImageRequest> images
) {}
