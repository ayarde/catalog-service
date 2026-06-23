package com.ecommerce.catalog.infrastructure.adapter.out.persistence.mongodb.document;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductImageDocumentTest {

    /**
    En esta prueba unitaria se espera que se valide el correcto funcionamiento
    de los setters y getters del documento de imagen.

    Características extras:
    - Se asignan valores a url, altText y sortOrder mediante setters.

    Se espera que el método:
    - Retorne los mismos valores asignados al invocar los getters correspondientes.
    **/
    @Test
    void setterAndGetter_ShouldRoundTrip() {
        //given
        ProductImageDocument doc = new ProductImageDocument();

        //when
        doc.setUrl("https://example.com/img.jpg");
        doc.setAltText("Alt text");
        doc.setSortOrder(1);

        //then
        assertThat(doc.getUrl()).isEqualTo("https://example.com/img.jpg");
        assertThat(doc.getAltText()).isEqualTo("Alt text");
        assertThat(doc.getSortOrder()).isEqualTo(1);
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
        ProductImageDocument doc = new ProductImageDocument();

        //then
        assertThat(doc.getUrl()).isNull();
        assertThat(doc.getAltText()).isNull();
        assertThat(doc.getSortOrder()).isNull();
    }
}
