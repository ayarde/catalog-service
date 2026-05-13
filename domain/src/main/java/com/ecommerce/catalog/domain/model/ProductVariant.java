package com.ecommerce.catalog.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ProductVariant(
        Long variantId,
        String sku,
        String variantName,
        BigDecimal price,
        String currency,
        Map<String, String> attributes,
        Integer stockQuantity,
        Integer lowStockThreshold,
        List<ProductImage> images
) {
    public ProductVariant {
        // Validaciones estructurales
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("Variant SKU is mandatory");
        if (stockQuantity != null && stockQuantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative");

        // Copias defensivas
        attributes = Map.copyOf(attributes != null ? attributes : Map.of());
        images = List.copyOf(images != null ? images : List.of());
    }

    // --- MANUAL BUILDER INFRASTRUCTURE ---

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .variantId(variantId)
                .sku(sku)
                .variantName(variantName)
                .price(price)
                .currency(currency)
                .attributes(attributes)
                .stockQuantity(stockQuantity)
                .lowStockThreshold(lowStockThreshold)
                .images(images);
    }

    public static class Builder {
        private Long variantId;
        private String sku;
        private String variantName;
        private BigDecimal price;
        private String currency;
        private Map<String, String> attributes;
        private Integer stockQuantity;
        private Integer lowStockThreshold;
        private List<ProductImage> images;

        public Builder variantId(Long variantId) { this.variantId = variantId; return this; }
        public Builder sku(String sku) { this.sku = sku; return this; }
        public Builder variantName(String variantName) { this.variantName = variantName; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder attributes(Map<String, String> attributes) { this.attributes = attributes; return this; }
        public Builder stockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; return this; }
        public Builder lowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; return this; }
        public Builder images(List<ProductImage> images) { this.images = images; return this; }

        public ProductVariant build() {
            return new ProductVariant(variantId, sku, variantName, price, currency,
                    attributes, stockQuantity, lowStockThreshold, images);
        }
    }
}
