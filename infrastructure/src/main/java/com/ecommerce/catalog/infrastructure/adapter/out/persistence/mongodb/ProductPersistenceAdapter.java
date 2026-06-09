package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb;

import com.ecommerce.catalog.domain.model.PagedResult;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document.ProductDocument;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.mapper.ProductPersistenceMapper;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.repository.SpringDataMongoProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final SpringDataMongoProductRepository mongoRepository;
    private final ProductPersistenceMapper mapper;

    public ProductPersistenceAdapter(SpringDataMongoProductRepository mongoRepository,
                                     ProductPersistenceMapper mapper) {
        this.mongoRepository = mongoRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductDocument document = mapper.toDocument(product);
        mongoRepository.findById(product.id()).ifPresent(existing -> {
            document.setVersion(existing.getVersion());
            document.markNotNew();
        });
        ProductDocument saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return mongoRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<Product> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDocument> docPage = mongoRepository.findAll(pageable);
        List<Product> products = docPage.getContent().stream().map(mapper::toDomain).toList();
        return new PagedResult<>(
                products,
                docPage.getNumber(),
                docPage.getSize(),
                docPage.getTotalElements(),
                docPage.getTotalPages()
        );
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return mongoRepository.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findByVariantId(Long variantId) {
        return mongoRepository.findByVariantsVariantId(variantId).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySku(String sku) {
        return mongoRepository.existsBySkuBase(sku);
    }

    @Override
    public void deleteById(Long id) {
        mongoRepository.deleteById(id);
    }
}
