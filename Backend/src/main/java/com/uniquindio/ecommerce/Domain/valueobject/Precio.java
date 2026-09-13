package com.uniquindio.ecommerce.Domain.valueobject;

public record Precio(double monto, String moneda) {

    public Precio {
        if (monto < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
    }
}