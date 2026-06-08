package com.ecommerce.catalog.infrastructure.adapter;

import com.ecommerce.catalog.domain.port.util.SlugGenerator;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

/**
 * Adaptador de Infraestructura para generar Slugs de forma manual (sin librerías externas).
 * Utiliza utilidades nativas de Java para limpiar acentos y caracteres especiales.
 */
@Component
public class SlugGeneratorAdapter implements SlugGenerator {

    @Override
    public String generate(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // 1. Convertir a minúsculas y quitar espacios en los extremos
        String noWhitespace = input.trim().toLowerCase();

        // 2. Normalizar (separa las vocales de sus acentos)
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);

        // 3. Eliminar los acentos (marcas diacríticas)
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 4. Reemplazar cualquier cosa que NO sea a-z o 0-9 por un guion '-'
        slug = slug.replaceAll("[^a-z0-9]+", "-");

        // 5. Eliminar guiones múltiples y guiones en los extremos
        slug = slug.replaceAll("-+", "-").replaceAll("^-+|-+$", "");

        return slug;
    }
}
