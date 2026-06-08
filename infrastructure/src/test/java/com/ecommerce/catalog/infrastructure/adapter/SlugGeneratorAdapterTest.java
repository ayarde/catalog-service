package com.ecommerce.catalog.infrastructure.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugGeneratorAdapterTest {

    private SlugGeneratorAdapter slugGenerator;

    @BeforeEach
    void setUp() {
        slugGenerator = new SlugGeneratorAdapter();
    }

    /**
    En esta prueba unitaria se espera que la generación de slug funcione correctamente
    con una cadena de texto simple estándar en inglés.

    Características extras:
    - Letras mayúsculas y minúsculas mixtas.
    - Espacios al inicio y al final.

    Se espera que el método:
    - Convierta a minúsculas.
    - Elimine espacios en los extremos.
    - Reemplace espacios intermedios por guiones simples.
    **/
    @Test
    void generate_WithStandardInput_ReturnsCleanSlug() {
        //given
        String input = "  My Awesome Product Name  ";

        //when
        String result = slugGenerator.generate(input);

        //then
        assertThat(result).isEqualTo("my-awesome-product-name");
    }

    /**
    En esta prueba unitaria se espera que el generador remueva correctamente marcas
    diacríticas y acentos del texto de entrada.

    Características extras:
    - Vocales en español con acento (á, é, í, ó, ú, ü).
    - Caracteres especiales como la ñ.

    Se espera que el método:
    - Normalice y descomponga los caracteres diacríticos.
    - Elimine los acentos dejando las letras base.
    **/
    @Test
    void generate_WithAccentedInput_ReturnsSlugWithoutAccents() {
        //given
        String input = "Café Orgánico de Montaña y Ñandú";

        //when
        String result = slugGenerator.generate(input);

        //then
        // Nota: La ñ se descompone y pierde la virgulilla si se usa Normalizer.Form.NFD
        // resultará en "cafe-organico-de-montana-y-nandu"
        assertThat(result).isEqualTo("cafe-organico-de-montana-y-nandu");
    }

    /**
    En esta prueba unitaria se espera que los caracteres especiales sean reemplazados
    por guiones de forma correcta sin duplicidades.

    Características extras:
    - Uso de símbolos: / @ + * & % $
    - Múltiples espacios consecutivos.

    Se espera que el método:
    - Reemplace cada grupo de caracteres no alfanuméricos por un solo guion.
    - No deje guiones repetidos ni guiones en los extremos.
    **/
    @Test
    void generate_WithSpecialCharacters_ReturnsCleanHyphenatedSlug() {
        //given
        String input = "iPhone 15 Pro Max // Black - 256GB @ Special!!";

        //when
        String result = slugGenerator.generate(input);

        //then
        assertThat(result).isEqualTo("iphone-15-pro-max-black-256gb-special");
    }

    /**
    En esta prueba unitaria se espera que se manejen correctamente las entradas nulas
    o en blanco sin lanzar excepciones.

    Características extras:
    - Entrada null.
    - Entrada vacía.
    - Entrada con solo espacios en blanco.

    Se espera que el método:
    - Retorne una cadena vacía en todos estos casos.
    **/
    @Test
    void generate_WithNullOrBlankInput_ReturnsEmptyString() {
        //given
        String nullInput = null;
        String emptyInput = "";
        String blankInput = "   ";

        //when
        String nullResult = slugGenerator.generate(nullInput);
        String emptyResult = slugGenerator.generate(emptyInput);
        String blankResult = slugGenerator.generate(blankInput);

        //then
        assertThat(nullResult).isEmpty();
        assertThat(emptyResult).isEmpty();
        assertThat(blankResult).isEmpty();
    }
}
