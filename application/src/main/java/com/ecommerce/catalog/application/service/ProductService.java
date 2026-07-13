package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.application.port.in.*;
import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.catalog.domain.exception.AlreadyExistsException;
import com.ecommerce.catalog.domain.exception.NotFoundException;
import com.ecommerce.catalog.domain.model.PagedResult;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductImage;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.domain.port.out.EventPublisher;
import com.ecommerce.catalog.domain.port.out.ProductRepository;
import com.ecommerce.catalog.domain.port.util.SlugGenerator;
import io.hypersistence.tsid.TSID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public class ProductService implements
        CreateProductUseCase, UpdateProductUseCase,
        ActivateProductUseCase, DeleteProductUseCase,
        GetProductUseCase, ListProductsUseCase,
        UpdateStockUseCase, GetProductBySlugUseCase,
        CheckVariantAvailabilityUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repository;
    private final EventPublisher eventPublisher;
    private final SlugGenerator slugGenerator;

    public ProductService(ProductRepository repository, EventPublisher eventPublisher, SlugGenerator slugGenerator) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.slugGenerator = slugGenerator;
    }

    @Override
    @Transactional
    @CacheEvict(value = "products_list", allEntries = true)
    public Product create(CreateProductCommand command) {
        if (repository.existsBySku(command.skuBase())) {
            throw new AlreadyExistsException("error.product.already_exists", command.skuBase());
        }

        Long tsid = TSID.Factory.getTsid().toLong();

        List<ProductVariant> variants = command.variants() == null ? List.of() :
                command.variants().stream()
                .map(req -> ProductVariant.builder()
                        .variantId(TSID.fast().toLong())
                        .sku(req.sku())
                        .variantName(req.variantName())
                        .price(req.price())
                        .currency(req.currency())
                        .stockQuantity(req.stockQuantity())
                        .build())
                .toList();

        List<ProductImage> images = command.images() == null ? List.of() :
                command.images().stream()
                .map(req -> ProductImage.builder()
                            .url(req.url())
                            .altText(req.altText())
                            .sortOrder(req.sortOrder())
                            .build())
                .toList();

        // Map Command -> Domain Entity (Initially DRAFT)
        String slugInput = command.name() + "-" + command.skuBase();
        
        var product = Product.builder()
                .id(tsid)
                .skuBase(command.skuBase())
                .name(command.name())
                .slug(slugGenerator.generate(slugInput))
                .description(command.description())
                .basePrice(command.basePrice())
                .currency(command.currency())
                .categories(command.categories())
                .tags(command.tags())
                .attributes(command.attributes())
                .variants(variants)
                .images(images)
                .status(ProductStatus.DRAFT)
                .domainEvents(List.of(new ProductCreatedEvent(tsid, command.skuBase(), command.name())))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        var savedProduct = repository.save(product);
        eventPublisher.publish(savedProduct.domainEvents());
        log.info("Product created successfully with ID: {} and SKU: {}", savedProduct.id(), savedProduct.skuBase());
        return savedProduct;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#p0.id()"),
            @CacheEvict(value = "product_slug", allEntries = true),
            @CacheEvict(value = "products_list", allEntries = true)
    })
    public Product update(UpdateProductCommand command) {
        var existingProduct = repository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("error.product.not_found", command.id()));

        var updatedProduct = existingProduct.updateDetails(
                command.name(), 
                command.description(),
                command.basePrice(),
                command.currency()
        );

        var savedProduct = repository.save(updatedProduct);
        eventPublisher.publish(savedProduct.domainEvents());
        return savedProduct;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#p0.id()"),
            @CacheEvict(value = "product_slug", allEntries = true),
            @CacheEvict(value = "products_list", allEntries = true)
    })
    public Product activate(ActivateProductCommand command) {
        var product = repository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("error.product.not_found", command.id()));

        log.info("Activating product ID: {}", command.id());
        var savedProduct = repository.save(product.activate());
        eventPublisher.publish(savedProduct.domainEvents());
        return savedProduct;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#p0.id()"),
            @CacheEvict(value = "product_slug", allEntries = true),
            @CacheEvict(value = "products_list", allEntries = true)
    })
    public void delete(DeleteProductCommand command) {
        var product = repository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("error.product.not_found", command.id()));

        log.info("Archiving product ID: {}", command.id());
        repository.save(product.archive());
    }

    @Override
    @Cacheable(value = "product", key = "#p0")
    public java.util.Optional<Product> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Cacheable(value = "products_list", key = "{#page, #size}")
    public PagedResult<Product> list(int page, int size) {
        return repository.findAll(page, size);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#p0"),
            @CacheEvict(value = "product_slug", key = "#result.slug()"),
            @CacheEvict(value = "products_list", allEntries = true)
    })
    public Product updateStock(Long productId, Long variantId, Integer newQuantity) {
        log.info("Updating stock for Product ID: {}, Variant ID: {} to {}", productId, variantId, newQuantity);

        var product = repository.findById(productId)
                .orElseThrow(() -> new NotFoundException("error.product.not_found", productId));

        var updatedProduct = product.updateVariantStock(variantId, newQuantity);

        if (product.status() != updatedProduct.status()) {
            log.info("Product Status changed from {} to {} due to stock update", 
                    product.status(), updatedProduct.status());
        }

        var savedProduct = repository.save(updatedProduct);
        eventPublisher.publish(savedProduct.domainEvents());
        return savedProduct;
    }

    @Override
    @Cacheable(value = "product_slug", key = "#p0")
    public java.util.Optional<Product> getBySlug(String slug) {
        log.info("Finding product by slug: {}", slug);
        return repository.findBySlug(slug);
    }

    @Override
    public java.util.Optional<VariantAvailability> checkAvailability(Long variantId) {
        log.info("Checking availability for variantId: {}", variantId);
        return repository.findByVariantId(variantId)
                .flatMap(product -> product.findVariantById(variantId)
                        .map(variant -> new VariantAvailability(
                                variant.variantId(),
                                product.id(),
                                product.name(),
                                variant.variantName(),
                                variant.sku(),
                                variant.stockQuantity() != null && variant.stockQuantity() > 0,
                                variant.stockQuantity() != null ? variant.stockQuantity() : 0,
                                product.status().name()
                        )));
    }
}
