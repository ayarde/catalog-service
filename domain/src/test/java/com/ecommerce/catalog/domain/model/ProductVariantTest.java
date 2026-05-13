package com.ecommerce.catalog.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductVariantTest {

    /**
    En esta prueba unitaria se valida que el constructor de ProductVariant
    valide los campos obligatorios.
    **/
    @Test
    void constructor_WithInvalidSku_ThrowsException() {
        //when & then
        ProductVariant.Builder builder = ProductVariant.builder().sku(null);
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
    En esta prueba unitaria se valida que el patrón toBuilder funcione correctamente
    permitiendo la inmutabilidad de la variante.
    **/
    @Test
    void toBuilder_Success() {
        //given
        ProductVariant variant = ProductVariant.builder()
                .variantId(1L)
                .sku("SKU-VAR-001")
                .price(new BigDecimal("10.00"))
                .currency("USD")
                .stockQuantity(100)
                .build();

        //when
        ProductVariant updatedVariant = variant.toBuilder()
                .stockQuantity(50)
                .build();

        //then
        assertThat(updatedVariant.variantId()).isEqualTo(variant.variantId());
        assertThat(updatedVariant.sku()).isEqualTo(variant.sku());
        assertThat(updatedVariant.stockQuantity()).isEqualTo(50);
    }
}
