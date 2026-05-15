package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.application.dto.ActivateProductCommand;
import com.ecommerce.catalog.domain.model.Product;

/**
 * Define el contrato para activar un producto que está en borrador.
 * Este paso es fundamental para que el producto sea visible y comprable en la tienda.
 */
public interface ActivateProductUseCase {
    Product activate(ActivateProductCommand command);
}
