package com.ecommerce.catalog.domain.port.out;

import com.ecommerce.catalog.domain.model.PagedResult;
import com.ecommerce.catalog.domain.model.Product;

import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long id);
    PagedResult<Product> findAll(int page, int size);
    Optional<Product> findBySlug(String slug);
    Optional<Product> findByVariantId(Long variantId);
    boolean existsBySku(String sku);
    void deleteById(Long id); // Note: Usually implementation performs soft delete
}

