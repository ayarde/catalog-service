package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
public class ProductDocument implements Persistable<Long> {

    @Id
    private Long id;
    @Version
    private Long version;
    @Indexed(unique = true)
    private String skuBase;
    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private String currency;
    private List<String> categories = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private Map<String, String> attributes = new HashMap<>();
    private String status;
    private List<ImageDocument> images = new ArrayList<>();
    private List<VariantDocument> variants = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    @Transient
    private boolean newEntity = true;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    public void markNotNew() {
        this.newEntity = false;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getSkuBase() {
        return skuBase;
    }

    public void setSkuBase(String skuBase) {
        this.skuBase = skuBase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ImageDocument> getImages() {
        return images;
    }

    public void setImages(List<ImageDocument> images) {
        this.images = images;
    }

    public List<VariantDocument> getVariants() {
        return variants;
    }

    public void setVariants(List<VariantDocument> variants) {
        this.variants = variants;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class ImageDocument {
        private String url;
        private String altText;
        private Integer sortOrder;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAltText() {
            return altText;
        }

        public void setAltText(String altText) {
            this.altText = altText;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    public static class VariantDocument {
        private Long variantId;
        private String sku;
        private String variantName;
        private BigDecimal price;
        private String currency;
        private Map<String, String> attributes = new HashMap<>();
        private Integer stockQuantity;
        private Integer lowStockThreshold;
        private List<ImageDocument> images = new ArrayList<>();

        public Long getVariantId() {
            return variantId;
        }

        public void setVariantId(Long variantId) {
            this.variantId = variantId;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }

        public Integer getLowStockThreshold() {
            return lowStockThreshold;
        }

        public void setLowStockThreshold(Integer lowStockThreshold) {
            this.lowStockThreshold = lowStockThreshold;
        }

        public List<ImageDocument> getImages() {
            return images;
        }

        public void setImages(List<ImageDocument> images) {
            this.images = images;
        }
    }
}
