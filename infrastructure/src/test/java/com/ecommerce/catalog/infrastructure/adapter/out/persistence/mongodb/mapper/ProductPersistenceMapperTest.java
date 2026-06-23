package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.mapper;

import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.model.ProductImage;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.domain.model.ProductVariant;
import com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document.ProductDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPersistenceMapperTest {

    private ProductPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductPersistenceMapper();
    }

    /**
    En esta prueba unitaria se espera que se valide el mapeo bidireccional de ida y vuelta (round-trip) entre la entidad de dominio Product y el documento ProductDocument.

    Características extras:
    - Incluye fechas de auditoría (createdAt, updatedAt), variantes e imágenes anidadas.
    - El mapeo preserva la integridad de todos los campos clave del agregado.

    Se espera que el método:
    - Convierta de Product a ProductDocument y luego retorne a Product preservando la equivalencia de los datos.
    - Mantenga las colecciones de variantes e imágenes sin pérdida de información.
    **/
    @Test
    void toDocumentAndToDomain_RoundTrip_PreservesCoreFields() {
        //given
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        Product product = Product.builder()
                .id(10L)
                .skuBase("SKU-MAP")
                .name("Mapped")
                .slug("mapped")
                .description("Desc")
                .basePrice(new BigDecimal("10.00"))
                .currency("EUR")
                .categories(List.of("c1"))
                .tags(List.of("t1"))
                .attributes(Map.of("k", "v"))
                .status(ProductStatus.ACTIVE)
                .variants(List.of(ProductVariant.builder()
                        .variantId(11L)
                        .sku("V-SKU")
                        .variantName("Var")
                        .price(new BigDecimal("10"))
                        .currency("EUR")
                        .stockQuantity(2)
                        .build()))
                .images(List.of(new ProductImage("https://cdn.example.com/i.jpg", "alt", 0)))
                .domainEvents(List.of())
                .createdAt(now)
                .updatedAt(now)
                .build();

        //when
        ProductDocument document = mapper.toDocument(product);
        Product restored = mapper.toDomain(document);

        //then
        assertThat(restored.id()).isEqualTo(10L);
        assertThat(restored.skuBase()).isEqualTo("SKU-MAP");
        assertThat(restored.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(restored.variants()).hasSize(1);
        assertThat(restored.images()).hasSize(1);
        assertThat(restored.domainEvents()).isEmpty();
    }

    /**
    En esta prueba unitaria se espera que el mapeo maneje correctamente un Product con status = null,
    ejerciendo la rama de status nulo en ambos sentidos (toDocument y toDomain).

    Características extras:
    - Product de dominio con status = null.
    - Documento MongoDB con status = null en el round-trip inverso.

    Se espera que el método:
    - El documento resultante tenga status = null.
    - El producto restaurado tenga status = null.
    **/
    @Test
    void toDocumentAndToDomain_WithNullStatus_ShouldHandleNull() {
        //given
        Product product = Product.builder()
                .id(20L)
                .skuBase("SKU-NULL")
                .name("No Status")
                .slug("no-status")
                .description("")
                .basePrice(new BigDecimal("5.00"))
                .currency("USD")
                .status(null)
                .domainEvents(List.of())
                .build();

        //when
        ProductDocument document = mapper.toDocument(product);
        Product restored = mapper.toDomain(document);

        //then
        assertThat(document.getStatus()).isNull();
        assertThat(restored.status()).isNull();
    }
}
