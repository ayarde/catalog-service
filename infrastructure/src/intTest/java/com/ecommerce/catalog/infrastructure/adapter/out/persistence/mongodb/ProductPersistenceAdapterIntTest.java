package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb;

import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductImage;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.repository.SpringDataMongoProductRepository;
import com.ecommerce.catalog.infrastructure.config.InfrastructureMongoIntTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MongoDBContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("inttest")
@SpringBootTest(classes = InfrastructureMongoIntTestConfig.class)
class ProductPersistenceAdapterIntTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @Autowired
    private ProductPersistenceAdapter adapter;

    @Autowired
    private SpringDataMongoProductRepository mongoRepository;

    @BeforeEach
    void cleanDatabase() {
        mongoRepository.deleteAll();
    }

    /**
    En esta prueba de integración se valida la persistencia inicial de un producto en MongoDB.

    Características extras:
    - El producto no existe previamente en la colección
    - Incluye variantes e imágenes anidadas

    Se espera que el método:
    - Inserte el documento correctamente
    - Permita recuperarlo por ID con los mismos datos relevantes
    **/
    @Test
    void save_WhenProductDoesNotExist_ShouldPersistAndBeRetrievable() {
        //given
        Product product = sampleProduct(1001L, "SKU-INT-1", "int-product-one", 201L);

        //when
        Product saved = adapter.save(product);
        Optional<Product> found = adapter.findById(saved.id());

        //then
        assertThat(found).isPresent();
        assertThat(found.get().skuBase()).isEqualTo("SKU-INT-1");
        assertThat(found.get().variants()).hasSize(1);
        assertThat(found.get().images()).hasSize(1);
    }

    /**
    En esta prueba de integración se valida la actualización optimista de un producto existente.

    Características extras:
    - El producto ya fue insertado previamente
    - Se modifica el nombre manteniendo el mismo ID

    Se espera que el método:
    - Actualice el documento sin error de bloqueo optimista
    - Refleje el nuevo nombre al consultar de nuevo
    **/
    @Test
    void save_WhenProductExists_ShouldUpdateWithVersion() {
        //given
        Product product = sampleProduct(1002L, "SKU-INT-2", "int-product-two", 202L);
        adapter.save(product);
        Product updated = product.toBuilder().name("Zapatillas Adidas Ultraboost 23").updatedAt(Instant.now()).build();

        //when
        adapter.save(updated);
        Optional<Product> found = adapter.findById(1002L);

        //then
        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Zapatillas Adidas Ultraboost 23");
    }

    /**
    En esta prueba de integración se valida la consulta por slug usando Spring Data Mongo.
    **/
    @Test
    void findBySlug_WhenExists_ShouldReturnProduct() {
        //given
        adapter.save(sampleProduct(1003L, "SKU-INT-3", "slug-lookup", 203L));

        //when
        Optional<Product> found = adapter.findBySlug("slug-lookup");

        //then
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(1003L);
    }

    /**
    En esta prueba de integración se valida la existencia por SKU base.
    **/
    @Test
    void existsBySku_WhenSkuExists_ShouldReturnTrue() {
        //given
        adapter.save(sampleProduct(1004L, "SKU-UNIQUE-4", "sku-exists", 204L));

        //when
        boolean exists = adapter.existsBySku("SKU-UNIQUE-4");

        //then
        assertThat(exists).isTrue();
    }

    /**
    En esta prueba de integración se valida la búsqueda por ID de variante anidada.
    **/
    @Test
    void findByVariantId_WhenVariantExists_ShouldReturnProduct() {
        //given
        Long variantId = 9999L;
        adapter.save(sampleProduct(1005L, "SKU-INT-5", "variant-lookup", variantId));

        //when
        Optional<Product> found = adapter.findByVariantId(variantId);

        //then
        assertThat(found).isPresent();
        assertThat(found.get().variants().getFirst().variantId()).isEqualTo(variantId);
    }

    /**
    En esta prueba de integración se valida el borrado físico del documento.
    **/
    @Test
    void deleteById_WhenExists_ShouldRemoveDocument() {
        //given
        adapter.save(sampleProduct(1006L, "SKU-INT-6", "to-delete", 206L));

        //when
        adapter.deleteById(1006L);

        //then
        assertThat(adapter.findById(1006L)).isEmpty();
        assertThat(mongoRepository.existsById(1006L)).isFalse();
    }

    // <editor-fold desc="Métodos auxiliares">
    private Product sampleProduct(Long id, String sku, String slug, Long variantId) {
        return Product.builder()
                .id(id)
                .skuBase(sku)
                .name("Integration Product")
                .slug(slug)
                .description("Integration description")
                .basePrice(new BigDecimal("49.99"))
                .currency("USD")
                .categories(List.of("cat"))
                .tags(List.of("tag"))
                .attributes(Map.of("brand", "test"))
                .status(ProductStatus.DRAFT)
                .variants(List.of(
                        ProductVariant.builder()
                                .variantId(variantId)
                                .sku("VAR-" + sku)
                                .variantName("Default")
                                .price(new BigDecimal("49.99"))
                                .currency("USD")
                                .stockQuantity(5)
                                .build()
                ))
                .images(List.of(new ProductImage("https://cdn.example.com/p.jpg", "main", 0)))
                .domainEvents(List.of())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    //</editor-fold>
}
