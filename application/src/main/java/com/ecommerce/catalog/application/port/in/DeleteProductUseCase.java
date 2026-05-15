package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.application.dto.DeleteProductCommand;

/**
 * Gestiona la eliminación de un producto del catálogo.
 * Internamente, el dominio procesa esta solicitud como un archivado para mantener la integridad histórica.
 */
public interface DeleteProductUseCase {
    void delete(DeleteProductCommand command);
}
