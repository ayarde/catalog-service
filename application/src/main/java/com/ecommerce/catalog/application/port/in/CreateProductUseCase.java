package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.application.dto.CreateProductCommand;
import com.ecommerce.catalog.domain.model.Product;

/**
 * Se encarga de dar de alta un nuevo producto en el sistema.
 * El resultado de esta operación suele ser un producto en estado de borrador (Draft)
 * pendiente de completar sus detalles comerciales.
 */
public interface CreateProductUseCase {
    Product create(CreateProductCommand command);
}

