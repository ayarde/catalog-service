package com.ecommerce.catalog.application.port.in;

import com.ecommerce.catalog.application.dto.VariantAvailability;

import java.util.Optional;

/**
 * Permite consultar si una variante específica tiene existencias suficientes.
 * Es un punto de integración clave, por ejemplo, para que el servicio de carrito
 * verifique la disponibilidad antes de confirmar una reserva.
 */
public interface CheckVariantAvailabilityUseCase {
    Optional<VariantAvailability> checkAvailability(Long variantId);
}
