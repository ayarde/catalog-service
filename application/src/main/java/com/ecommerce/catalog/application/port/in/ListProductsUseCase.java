package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.domain.model.PagedResult;
import com.ecommerce.catalog.domain.model.Product;

/**
 * Recupera el listado paginado de productos disponibles en el catálogo.
 */
public interface ListProductsUseCase {
    PagedResult<Product> list(int page, int size);
}
