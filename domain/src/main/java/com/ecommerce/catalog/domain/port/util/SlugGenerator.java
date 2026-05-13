package com.ecommerce.catalog.domain.port.util;

/**
 * Puerto para la generación de Slugs amigables para SEO a partir de texto.
 * Ejemplo: "Samsung Galaxy S24" -> "samsung-galaxy-s24"
 */
public interface SlugGenerator {
    String generate(String input);
}
