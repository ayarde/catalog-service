package com.ecommerce.catalog.domain.model;

import com.ecommerce.catalog.domain.event.ProductActivatedEvent;
import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.catalog.domain.event.ProductPriceChangedEvent;
import com.ecommerce.catalog.domain.event.ProductStockChangedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    /**
    En esta prueba unitaria se espera que el constructor del Product valide correctamente
    los campos obligatorios (Nivel 1: Validación Estructural).

    Características extras:
    - Se prueba con ID nulo
    - Se prueba con SKU vacío
    - Se prueba con Slug inválido

    Se espera que el método:
    - Lance IllegalArgumentException o NullPointerException según el campo
    **/
    @Test
    void constructor_WithInvalidFields_ThrowsException() {
        //given
        Long id = null;
        String sku = "";

        //when & then
        Product.Builder idBuilder = createBaseProductBuilder().id(id);
        assertThatThrownBy(idBuilder::build)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ID is mandatory");

        Product.Builder skuBuilder = createBaseProductBuilder().skuBase(sku);
        assertThatThrownBy(skuBuilder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("skuBase is mandatory");

        Product.Builder slugBuilder = createBaseProductBuilder().slug("Slug con espacios");
        assertThatThrownBy(slugBuilder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid slug format");
    }

    /**
    En esta prueba unitaria se espera que un producto pueda activarse exitosamente
    si cumple con todas las reglas de negocio necesarias.

    Características extras:
    - El producto tiene precio base y moneda
    - El producto tiene al menos una variante e imagen

    Se espera que el método:
    - Cambie el estado a ACTIVE
    - Genere un evento ProductActivatedEvent
    - Actualice la fecha de modificación
    **/
    @Test
    void activate_Success() {
        //given
        Product product = createValidDraftProduct();

        //when
        Product activatedProduct = product.activate();

        //then
        assertThat(activatedProduct.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(activatedProduct.updatedAt()).isAfter(product.updatedAt());
        assertThat(activatedProduct.domainEvents()).hasSize(1);
        assertThat(activatedProduct.domainEvents().get(0)).isInstanceOf(ProductActivatedEvent.class);
    }

    /**
    En esta prueba unitaria se espera que la activación falle si el producto no tiene variantes.

    Características extras:
    - El producto está en estado DRAFT
    - La lista de variantes está vacía

    Se espera que el método:
    - Lance IllegalStateException
    - El mensaje mencione que se requiere al menos una variante
    **/
    @Test
    void activate_WithoutVariants_ThrowsIllegalStateException() {
        //given
        Product product = createBaseProductBuilder()
                .variants(List.of())
                .status(ProductStatus.DRAFT)
                .build();

        //when & then
        assertThatThrownBy(product::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("At least one variant is required for activation");
    }

    /**
    En esta prueba unitaria se espera que la actualización de detalles del producto
    funcione correctamente y genere el evento de cambio de precio.

    Se espera que el método:
    - Actualice nombre, descripción y precio
    - Genere un evento ProductPriceChangedEvent
    - Mantenga la inmutabilidad (devuelva nueva instancia)
    **/
    @Test
    void updateDetails_Success() {
        //given
        Product product = createValidDraftProduct();
        String newName = "New Name";
        BigDecimal newPrice = new BigDecimal("150.00");

        //when
        Product updatedProduct = product.updateDetails(newName, "New Description", newPrice, "USD");

        //then
        assertThat(updatedProduct.name()).isEqualTo(newName);
        assertThat(updatedProduct.basePrice()).isEqualTo(newPrice);
        assertThat(updatedProduct.domainEvents()).hasSize(1);
        assertThat(updatedProduct.domainEvents().get(0)).isInstanceOf(ProductPriceChangedEvent.class);
    }

    /**
    En esta prueba unitaria se espera que al actualizar el stock de una variante,
    el producto recalcule su stock total y cambie su estado a ACTIVE si antes estaba agotado.

    Características extras:
    - El producto empieza con stock 0 (OUT_OF_STOCK)
    - Se actualiza una variante a stock 10

    Se espera que el método:
    - Cambie el estado de OUT_OF_STOCK a ACTIVE
    - Actualice el stock de la variante específica
    - Genere un evento ProductStockChangedEvent
    **/
    @Test
    void updateVariantStock_FromOutOfStockToActive() {
        //given
        Long variantId = 100L;
        ProductVariant variant = ProductVariant.builder()
                .variantId(variantId)
                .sku("VAR-001")
                .stockQuantity(0)
                .build();
        
        Product product = createBaseProductBuilder()
                .variants(List.of(variant))
                .status(ProductStatus.OUT_OF_STOCK)
                .build();

        //when
        Product updatedProduct = product.updateVariantStock(variantId, 10);

        //then
        assertThat(updatedProduct.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(updatedProduct.getTotalStock()).isEqualTo(10);
        assertThat(updatedProduct.domainEvents()).hasSize(1);
        assertThat(updatedProduct.domainEvents().get(0)).isInstanceOf(ProductStockChangedEvent.class);
    }

    /**
    En esta prueba unitaria se espera que el producto pase a estado OUT_OF_STOCK
    si todas sus variantes se quedan sin stock.

    Se espera que el método:
    - Cambie el estado de ACTIVE a OUT_OF_STOCK
    - Recalcule el stock total a 0
    **/
    @Test
    void updateVariantStock_ToOutOfStock_WhenTotalStockIsZero() {
        //given
        Long variantId = 100L;
        ProductVariant variant = ProductVariant.builder()
                .variantId(variantId)
                .sku("VAR-001")
                .stockQuantity(5)
                .build();

        Product product = createBaseProductBuilder()
                .variants(List.of(variant))
                .status(ProductStatus.ACTIVE)
                .build();

        //when
        Product updatedProduct = product.updateVariantStock(variantId, 0);

        //then
        assertThat(updatedProduct.status()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        assertThat(updatedProduct.getTotalStock()).isEqualTo(0);
    }

    /**
    En esta prueba unitaria se espera que la creación de un producto mediante el método
    estático de fábrica funcione correctamente.

    Se espera que el método:
    - Inicialice el producto en estado DRAFT
    - Genere un evento ProductCreatedEvent con los datos correctos
    - Asigne las fechas de creación y actualización
    **/
    @Test
    void create_Success() {
        //given
        Long id = 1L;
        String sku = "SKU-001";
        String name = "Product Name";
        String slug = "product-name";

        //when
        Product product = Product.create(id, sku, name, slug);

        //then
        assertThat(product.status()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.skuBase()).isEqualTo(sku);
        assertThat(product.domainEvents()).hasSize(1);
        assertThat(product.domainEvents().get(0)).isInstanceOf(ProductCreatedEvent.class);
        assertThat(product.createdAt()).isNotNull();
        assertThat(product.updatedAt()).isNotNull();
    }

    /**
    En esta prueba unitaria se espera que el archivado de un producto cambie su estado correctamente.

    Se espera que el método:
    - Cambie el estado a ARCHIVED
    - Actualice la fecha de modificación
    **/
    @Test
    void archive_Success() {
        //given
        Product product = createValidDraftProduct();

        //when
        Product archivedProduct = product.archive();

        //then
        assertThat(archivedProduct.status()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(archivedProduct.updatedAt()).isAfter(product.updatedAt());
    }

    /**
    En esta prueba unitaria se espera poder encontrar una variante específica por su ID.
    **/
    @Test
    void findVariantById_Found() {
        //given
        Long variantId = 555L;
        ProductVariant variant = ProductVariant.builder().variantId(variantId).sku("V1").build();
        Product product = createBaseProductBuilder().variants(List.of(variant)).build();

        //when
        Optional<ProductVariant> result = product.findVariantById(variantId);

        //then
        assertThat(result).isPresent();
        assertThat(result.get().variantId()).isEqualTo(variantId);
    }

    //<editor-fold desc="Métodos auxiliares">
    private Product.Builder createBaseProductBuilder() {
        return Product.builder()
                .id(1L)
                .skuBase("PROD-001")
                .name("Test Product")
                .slug("test-product")
                .basePrice(new BigDecimal("100.00"))
                .currency("USD")
                .status(ProductStatus.DRAFT)
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
    }

    private Product createValidDraftProduct() {
        ProductVariant variant = ProductVariant.builder()
                .variantId(101L)
                .sku("SKU-VAR-1")
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .stockQuantity(10)
                .build();

        ProductImage image = ProductImage.builder()
                .url("https://example.com/image.jpg")
                .altText("Image")
                .sortOrder(0)
                .build();

        return createBaseProductBuilder()
                .variants(List.of(variant))
                .images(List.of(image))
                .build();
    }
    //</editor-fold>
}
