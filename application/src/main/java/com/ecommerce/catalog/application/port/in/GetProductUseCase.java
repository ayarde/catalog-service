package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.domain.model.Product;
import java.util.Optional;

/**
 * Obtiene el detalle completo de un producto mediante su identificador único (ID).
 */
public interface GetProductUseCase {
    Optional<Product> getById(Long id);
}
