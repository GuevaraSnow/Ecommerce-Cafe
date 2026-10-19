package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.LocalDate;

/**
 * Descuento por temporada que el Vendedor programa sobre una Presentación:
 * un porcentaje entre 1 y 50 que rige entre dos fechas, ambas incluidas.
 */
public record Descuento(int porcentaje, LocalDate fechaInicio, LocalDate fechaFin) {

    public static final int PORCENTAJE_MINIMO = 1;
    public static final int PORCENTAJE_MAXIMO = 50;

    /**
     * @throws ReglaDominioException si el porcentaje sale del rango permitido, falta alguna fecha
     *                               o la fecha de fin es anterior a la de inicio
     */
    public Descuento {
        if (porcentaje < PORCENTAJE_MINIMO || porcentaje > PORCENTAJE_MAXIMO) {
            throw new ReglaDominioException("El porcentaje de descuento debe estar entre "
                    + PORCENTAJE_MINIMO + " y " + PORCENTAJE_MAXIMO);
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaDominioException("El descuento debe tener fecha de inicio y fecha de fin");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ReglaDominioException("La fecha de fin del descuento no puede ser anterior a la de inicio");
        }
    }

    /** Indica si el descuento rige en esa fecha: desde el día de inicio hasta el de fin, ambos incluidos. */
    public boolean estaVigenteEn(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }
}
