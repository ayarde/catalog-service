package com.ecommerce.catalog.application.dto;

public record ImageRequest(
        String url,
        String altText,
        Integer sortOrder
) {}
