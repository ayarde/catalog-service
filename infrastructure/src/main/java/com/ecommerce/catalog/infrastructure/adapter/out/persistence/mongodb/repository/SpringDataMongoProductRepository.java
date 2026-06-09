package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.repository;

import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document.ProductDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataMongoProductRepository extends MongoRepository<ProductDocument, Long> {

    Optional<ProductDocument> findBySlug(String slug);

    Optional<ProductDocument> findByVariantsVariantId(Long variantId);

    boolean existsBySkuBase(String skuBase);
}
