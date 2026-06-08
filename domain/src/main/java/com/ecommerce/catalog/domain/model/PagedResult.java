package com.ecommerce.catalog.domain.model;

import java.util.List;

/**
 * Representa un resultado paginado genérico dentro del modelo de dominio.
 * Mantiene la independencia tecnológica al no acoplarse con frameworks específicos.
 *
 * @param <T> Tipo de elemento contenido en la página.
 */
public record PagedResult<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
