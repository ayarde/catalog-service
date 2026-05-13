package com.ecommerce.catalog.domain.model;

import com.ecommerce.catalog.domain.event.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record Product(
        Long id,
        String skuBase,
        String name,
        String slug,
        String description,
        BigDecimal basePrice,
        String currency,
        List<String> categories,
        List<String> tags,
        Map<String, String> attributes,
        ProductStatus status,
        List<ProductImage> images,
        List<ProductVariant> variants,
        List<DomainEvent> domainEvents,
        Instant createdAt,
        Instant updatedAt
) {
    public Product {
        // Structural Validation (Level 1)
        Objects.requireNonNull(id, "ID is mandatory");
        if (skuBase == null || skuBase.isBlank()) throw new IllegalArgumentException("skuBase is mandatory");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is mandatory");
        if (slug == null || !slug.matches("^[a-z0-9-]+$")) throw new IllegalArgumentException("Invalid slug format");

        // Defensive copies for immutability
        categories = List.copyOf(categories != null ? categories : List.of());
        tags = List.copyOf(tags != null ? tags : List.of());
        attributes = Map.copyOf(attributes != null ? attributes : Map.of());
        images = List.copyOf(images != null ? images : List.of());
        variants = List.copyOf(variants != null ? variants : List.of());
        domainEvents = List.copyOf(domainEvents != null ? domainEvents : List.of());
    }

    public static Product create(Long id, String skuBase, String name, String slug) {
        return Product.builder()
                .id(id)
                .skuBase(skuBase)
                .name(name)
                .slug(slug)
                .status(ProductStatus.DRAFT)
                .domainEvents(List.of(new ProductCreatedEvent(id, skuBase, name)))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void validateForActivation() {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Valid base price is required for activation");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalStateException("Currency is required for activation");
        }
        if (variants.isEmpty()) {
            throw new IllegalStateException("At least one variant is required for activation");
        }
        if (images.isEmpty()) {
            throw new IllegalStateException("At least one image is required for activation");
        }
    }

    public Product activate() {
        validateForActivation();

        return this.toBuilder()
                .status(ProductStatus.ACTIVE)
                .domainEvents(List.of(new ProductActivatedEvent(this.id, this.slug, this.basePrice)))
                .updatedAt(Instant.now())
                .build();
    }

    public Product updateDetails(
            String newName,
            String newDescription,
            BigDecimal newBasePrice,
            String newCurrency
    ) {
        // 1. Lógica de Negocio: Si el nombre cambia, podrías decidir NO cambiar el slug
        // para no romper el SEO de los enlaces ya compartidos.
        return this.toBuilder()
                .name(newName)
                .description(newDescription)
                .basePrice(newBasePrice)
                .currency(newCurrency)
                .domainEvents(List.of(new ProductPriceChangedEvent(this.id, newBasePrice, newCurrency)))
                .updatedAt(Instant.now())
                .build();
    }

    public Product archive() {
        return this.toBuilder()
                .status(ProductStatus.ARCHIVED)
                .updatedAt(Instant.now())
                .build();
    }

    public Product updateVariantStock(Long variantId, Integer newQuantity) {
        // 1. Buscamos y actualizamos la variante (inmutabilidad)
        List<ProductVariant> updatedVariants = this.variants.stream()
                .map(v -> v.variantId().equals(variantId) 
                        ? v.toBuilder().stockQuantity(newQuantity).build() 
                        : v)
                .toList();

        int totalStock = updatedVariants.stream()
                .mapToInt(v -> v.stockQuantity() != null ? v.stockQuantity() : 0)
                .sum();

        // 2. Aplicamos la máquina de estados según el stock total
        ProductStatus newStatus = this.status;
        if (this.status == ProductStatus.ACTIVE && totalStock == 0) {
            newStatus = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK && totalStock > 0) {
            newStatus = ProductStatus.ACTIVE;
        }

        return this.toBuilder()
                .variants(updatedVariants)
                .status(newStatus)
                .domainEvents(List.of(new ProductStockChangedEvent(this.id, variantId, newQuantity, newStatus.name())))
                .updatedAt(Instant.now())
                .build();
    }

    public int getTotalStock() {
        return this.variants.stream()
                .mapToInt(v -> v.stockQuantity() != null ? v.stockQuantity() : 0)
                .sum();
    }

    /**
     * Busca una variante específica por su ID.
     * Mantiene la lógica de navegación dentro del dominio (Tell, Don't Ask).
     */
    public Optional<ProductVariant> findVariantById(Long variantId) {
        return this.variants.stream()
                .filter(v -> v.variantId().equals(variantId))
                .findFirst();
    }

    // --- MANUAL BUILDER INFRASTRUCTURE ---

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .skuBase(skuBase)
                .name(name)
                .slug(slug)
                .description(description)
                .basePrice(basePrice)
                .currency(currency)
                .categories(categories)
                .tags(tags)
                .attributes(attributes)
                .status(status)
                .images(images)
                .variants(variants)
                .domainEvents(domainEvents)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    public static class Builder {
        private Long id;
        private String skuBase;
        private String name;
        private String slug;
        private String description;
        private BigDecimal basePrice;
        private String currency;
        private List<String> categories;
        private List<String> tags;
        private Map<String, String> attributes;
        private ProductStatus status;
        private List<ProductImage> images;
        private List<ProductVariant> variants;
        private List<DomainEvent> domainEvents;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder skuBase(String skuBase) { this.skuBase = skuBase; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder basePrice(BigDecimal basePrice) { this.basePrice = basePrice; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder categories(List<String> categories) { this.categories = categories; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder attributes(Map<String, String> attributes) { this.attributes = attributes; return this; }
        public Builder status(ProductStatus status) { this.status = status; return this; }
        public Builder images(List<ProductImage> images) { this.images = images; return this; }
        public Builder variants(List<ProductVariant> variants) { this.variants = variants; return this; }
        public Builder domainEvents(List<DomainEvent> domainEvents) { this.domainEvents = domainEvents; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Product build() {
            return new Product(id, skuBase, name, slug, description, basePrice, currency,
                    categories, tags, attributes, status, images, variants, domainEvents, createdAt, updatedAt);
        }
    }
}
