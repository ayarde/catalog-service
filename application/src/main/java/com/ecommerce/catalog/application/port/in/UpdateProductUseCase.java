package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.application.dto.UpdateProductCommand;
import com.ecommerce.catalog.domain.model.Product;

/**
 * Permite modificar la información de un producto existente.
 * Se utiliza para actualizar descripciones, nombres o precios base dentro de un flujo controlado.
 */
public interface UpdateProductUseCase {
    Product update(UpdateProductCommand command);
}
