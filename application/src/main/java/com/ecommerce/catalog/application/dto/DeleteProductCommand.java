package com.ecommerce.catalog.application.dto;

public record DeleteProductCommand(
        Long id,
        String reason
) {}
