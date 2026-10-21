package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;

/**
 * Criterios opcionales con los que el Comprador busca Presentaciones: un criterio
 * en {@code null} no filtra. El rango de precio se aplica al Precio normal.
 */
public record FiltroDePresentaciones(String titulo, TipoDePresentacion tipo, String vendedorId,
                                     Double precioMinimo, Double precioMaximo) {

    /** @throws ReglaDominioException si el precio mínimo es mayor que el máximo */
    public FiltroDePresentaciones {
        if (precioMinimo != null && precioMaximo != null && precioMinimo > precioMaximo) {
            throw new ReglaDominioException("El precio mínimo no puede ser mayor que el máximo");
        }
    }
}
