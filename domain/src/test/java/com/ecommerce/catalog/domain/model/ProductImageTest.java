package com.ecommerce.catalog.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductImageTest {

    /**
    En esta prueba unitaria se valida que el constructor de ProductImage
    realice las validaciones estructurales necesarias.
    **/
    @Test
    void constructor_WithInvalidUrl_ThrowsException() {
        //when & then
        ProductImage.Builder builder = ProductImage.builder().url(null);
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
    En esta prueba unitaria se valida el funcionamiento de toBuilder
    para asegurar que la inmutabilidad se mantenga al actualizar campos.
    **/
    @Test
    void toBuilder_Success() {
        //given
        ProductImage image = ProductImage.builder()
                .url("https://example.com/1.jpg")
                .altText("Old Alt")
                .sortOrder(1)
                .build();

        //when
        ProductImage updatedImage = image.toBuilder()
                .altText("New Alt")
                .build();

        //then
        assertThat(updatedImage.url()).isEqualTo(image.url());
        assertThat(updatedImage.altText()).isEqualTo("New Alt");
        assertThat(updatedImage.sortOrder()).isEqualTo(1);
    }
}
