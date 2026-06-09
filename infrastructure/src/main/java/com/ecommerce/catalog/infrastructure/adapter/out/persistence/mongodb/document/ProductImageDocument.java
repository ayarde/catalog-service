package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document;

/**
 * Sub-documento para MongoDB que mapea un ProductImage.
 */
public class ProductImageDocument {
    private String url;
    private String altText;
    private Integer sortOrder;

    public ProductImageDocument() {}

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
