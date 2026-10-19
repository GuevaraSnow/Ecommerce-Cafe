package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/**
 * Precio de venta de una Presentación: un monto positivo junto con la
 * moneda en que se expresa.
 */
public record Precio(double monto, String moneda) {

    /**
     * @throws ReglaDominioException si el monto no es mayor que cero o si no se especifica una moneda válida
     */
    public Precio {
        if (monto <= 0) {
            throw new ReglaDominioException("El precio no puede ser negativo o cero");
        }
        if (moneda == null || moneda.isBlank()) {
            throw new ReglaDominioException("El precio debe especificar una moneda válida");
        }
    }

    /**
     * Devuelve un nuevo Precio con el descuento aplicado, redondeado a centavos.
     * Este Precio no cambia: es inmutable.
     *
     * @throws ReglaDominioException si el descuento es nulo
     */
    public Precio aplicarDescuento(Descuento descuento) {
        if (descuento == null) {
            throw new ReglaDominioException("El descuento a aplicar no puede ser nulo");
        }
        double montoConDescuento = monto * (100 - descuento.porcentaje()) / 100.0;
        return new Precio(Math.round(montoConDescuento * 100) / 100.0, moneda);
    }
}