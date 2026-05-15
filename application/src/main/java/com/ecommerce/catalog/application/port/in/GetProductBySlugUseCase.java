package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.domain.model.Product;
import java.util.Optional;

/**
 * Busca un producto utilizando su identificador amigable o 'slug'.
 * Es el puerto ideal para las búsquedas desde la interfaz de usuario (SEO) donde no se dispone del ID técnico.
 */
public interface GetProductBySlugUseCase {
    Optional<Product> getBySlug(String slug);
}
