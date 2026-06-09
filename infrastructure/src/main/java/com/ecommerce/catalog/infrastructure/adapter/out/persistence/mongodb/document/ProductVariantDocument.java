package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Sub-documento para MongoDB que mapea un ProductVariant.
 */
public class ProductVariantDocument {
    private Long variantId;
    private String sku;
    private String variantName;
    private BigDecimal price;
    private String currency;
    private Integer stockQuantity;
    private Map<String, String> attributes;

    public ProductVariantDocument() {}

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
}
