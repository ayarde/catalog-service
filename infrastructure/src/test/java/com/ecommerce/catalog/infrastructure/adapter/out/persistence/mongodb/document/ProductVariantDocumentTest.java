package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProductVariantDocumentTest {

    /**
    En esta prueba unitaria se espera que se valide el correcto funcionamiento
    de los setters y getters del documento de variante.

    Características extras:
    - Se asignan valores a todos los campos mediante setters.
    - Incluye atributos como mapa de clave-valor.

    Se espera que el método:
    - Retorne los mismos valores asignados al invocar los getters correspondientes.
    **/
    @Test
    void setterAndGetter_ShouldRoundTrip() {
        //given
        ProductVariantDocument doc = new ProductVariantDocument();

        //when
        doc.setVariantId(1L);
        doc.setSku("VAR-SKU");
        doc.setVariantName("Red / L");
        doc.setPrice(new BigDecimal("25.00"));
        doc.setCurrency("USD");
        doc.setStockQuantity(10);
        doc.setAttributes(Map.of("color", "red"));

        //then
        assertThat(doc.getVariantId()).isEqualTo(1L);
        assertThat(doc.getSku()).isEqualTo("VAR-SKU");
        assertThat(doc.getVariantName()).isEqualTo("Red / L");
        assertThat(doc.getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(doc.getCurrency()).isEqualTo("USD");
        assertThat(doc.getStockQuantity()).isEqualTo(10);
        assertThat(doc.getAttributes()).containsEntry("color", "red");
    }

    /**
    En esta prueba unitaria se espera que el constructor por defecto
    inicialice todos los campos en null.

    Características extras:
    - No se asignan valores después de la creación.

    Se espera que el método:
    - Retorne null para todos los getters.
    **/
    @Test
    void defaultConstructor_ShouldCreateEmptyInstance() {
        //when
        ProductVariantDocument doc = new ProductVariantDocument();

        //then
        assertThat(doc.getVariantId()).isNull();
        assertThat(doc.getSku()).isNull();
        assertThat(doc.getPrice()).isNull();
        assertThat(doc.getStockQuantity()).isNull();
        assertThat(doc.getAttributes()).isNull();
    }
}
