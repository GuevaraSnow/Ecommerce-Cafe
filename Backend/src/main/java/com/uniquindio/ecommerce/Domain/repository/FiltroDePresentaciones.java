package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;

/**
 * Criterios opcionales con los que el Comprador busca Presentaciones: un criterio
 * en {@code null} no filtra. El rango de precio se aplica al Precio normal. La calificación mínima se
 * compara con el promedio de las Reseñas de la Presentación: una Presentación sin Reseñas no tiene
 * promedio y queda fuera cuando se pide una calificación mínima.
 */
public record FiltroDePresentaciones(String titulo, TipoDePresentacion tipo, String vendedorId,
                                     Double precioMinimo, Double precioMaximo, Double calificacionMinima) {

    /**
     * @throws ReglaDominioException si el precio mínimo es mayor que el máximo o si la calificación mínima
     *                               está fuera del rango de 1 a 5
     */
    public FiltroDePresentaciones {
        if (precioMinimo != null && precioMaximo != null && precioMinimo > precioMaximo) {
            throw new ReglaDominioException("El precio mínimo no puede ser mayor que el máximo");
        }
        if (calificacionMinima != null
                && (calificacionMinima < Calificacion.MINIMO || calificacionMinima > Calificacion.MAXIMO)) {
            throw new ReglaDominioException("La calificación mínima debe estar entre " + Calificacion.MINIMO
                    + " y " + Calificacion.MAXIMO + ": " + calificacionMinima);
        }
    }

    /** Filtro sin calificación mínima. */
    public FiltroDePresentaciones(String titulo, TipoDePresentacion tipo, String vendedorId,
                                  Double precioMinimo, Double precioMaximo) {
        this(titulo, tipo, vendedorId, precioMinimo, precioMaximo, null);
    }
}
