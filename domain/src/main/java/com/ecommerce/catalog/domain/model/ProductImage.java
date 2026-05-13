package com.ecommerce.catalog.domain.model;

public record ProductImage(
        String url,
        String altText,
        Integer sortOrder
) {
    public ProductImage {
        // Validaciones estructurales
        if (url == null || !url.startsWith("https://")) {
            throw new IllegalArgumentException("The URL should start with https://");
        }
        if (altText == null || altText.isBlank()) {
            throw new IllegalArgumentException("Alternative text is required");
        }
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }
    }

    // --- MANUAL BUILDER INFRASTRUCTURE ---

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .url(url)
                .altText(altText)
                .sortOrder(sortOrder);
    }

    public static class Builder {
        private String url;
        private String altText;
        private Integer sortOrder;

        public Builder url(String url) { this.url = url; return this; }
        public Builder altText(String altText) { this.altText = altText; return this; }
        public Builder sortOrder(Integer sortOrder) { this.sortOrder = sortOrder; return this; }

        public ProductImage build() {
            return new ProductImage(url, altText, sortOrder);
        }
    }
}
