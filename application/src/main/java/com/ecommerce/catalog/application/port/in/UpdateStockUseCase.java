package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.domain.model.Product;

/**
 * Puerto encargado de actualizar los niveles de inventario para una variante específica de un producto.
 * Esta operación es crítica para sincronizar el stock físico con la disponibilidad en la tienda online.
 */
public interface UpdateStockUseCase {
    Product updateStock(Long productId, Long variantId, Integer newQuantity);
}
