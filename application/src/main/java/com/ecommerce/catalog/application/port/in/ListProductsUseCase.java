package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.domain.model.Product;
import java.util.List;

/**
 * Recupera el listado completo de productos disponibles en el catálogo.
 */
public interface ListProductsUseCase {
    List<Product> listAll();
}
