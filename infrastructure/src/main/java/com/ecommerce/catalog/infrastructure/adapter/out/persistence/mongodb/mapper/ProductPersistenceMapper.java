package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.mapper;

import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductImage;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document.ProductDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductPersistenceMapper {

    public ProductDocument toDocument(Product product) {
        ProductDocument document = new ProductDocument();
        document.setId(product.id());
        document.setSkuBase(product.skuBase());
        document.setName(product.name());
        document.setSlug(product.slug());
        document.setDescription(product.description());
        document.setBasePrice(product.basePrice());
        document.setCurrency(product.currency());
        document.setCategories(product.categories());
        document.setTags(product.tags());
        document.setAttributes(product.attributes());
        document.setStatus(product.status() != null ? product.status().name() : null);
        document.setCreatedAt(product.createdAt());
        document.setUpdatedAt(product.updatedAt());
        document.setImages(product.images().stream().map(this::toImageDocument).collect(Collectors.toList()));
        document.setVariants(product.variants().stream().map(this::toVariantDocument).collect(Collectors.toList()));
        return document;
    }

    public Product toDomain(ProductDocument document) {
        return Product.builder()
                .id(document.getId())
                .skuBase(document.getSkuBase())
                .name(document.getName())
                .slug(document.getSlug())
                .description(document.getDescription())
                .basePrice(document.getBasePrice())
                .currency(document.getCurrency())
                .categories(document.getCategories())
                .tags(document.getTags())
                .attributes(document.getAttributes())
                .status(document.getStatus() != null ? ProductStatus.valueOf(document.getStatus()) : null)
                .images(document.getImages().stream().map(this::toImage).collect(Collectors.toList()))
                .variants(document.getVariants().stream().map(this::toVariant).collect(Collectors.toList()))
                .domainEvents(List.of())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    private ProductDocument.ImageDocument toImageDocument(ProductImage image) {
        ProductDocument.ImageDocument doc = new ProductDocument.ImageDocument();
        doc.setUrl(image.url());
        doc.setAltText(image.altText());
        doc.setSortOrder(image.sortOrder());
        return doc;
    }

    private ProductImage toImage(ProductDocument.ImageDocument doc) {
        return new ProductImage(doc.getUrl(), doc.getAltText(), doc.getSortOrder());
    }

    private ProductDocument.VariantDocument toVariantDocument(ProductVariant variant) {
        ProductDocument.VariantDocument doc = new ProductDocument.VariantDocument();
        doc.setVariantId(variant.variantId());
        doc.setSku(variant.sku());
        doc.setVariantName(variant.variantName());
        doc.setPrice(variant.price());
        doc.setCurrency(variant.currency());
        doc.setAttributes(variant.attributes());
        doc.setStockQuantity(variant.stockQuantity());
        doc.setLowStockThreshold(variant.lowStockThreshold());
        doc.setImages(variant.images().stream().map(this::toImageDocument).collect(Collectors.toList()));
        return doc;
    }

    private ProductVariant toVariant(ProductDocument.VariantDocument doc) {
        return ProductVariant.builder()
                .variantId(doc.getVariantId())
                .sku(doc.getSku())
                .variantName(doc.getVariantName())
                .price(doc.getPrice())
                .currency(doc.getCurrency())
                .attributes(doc.getAttributes())
                .stockQuantity(doc.getStockQuantity())
                .lowStockThreshold(doc.getLowStockThreshold())
                .images(doc.getImages().stream().map(this::toImage).collect(Collectors.toList()))
                .build();
    }
}
