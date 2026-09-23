package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record DetalleCompra(String compraId, String presentacionId, PrecioCongelado precioCongelado, Cantidad cantidad) {
    public DetalleCompra{
        if (compraId == null || compraId.isBlank()) {
            throw new ReglaDominioException("No esta asociado a ninguna compra");
        }
        if (presentacionId == null || presentacionId.isBlank()) {
            throw  new ReglaDominioException("No esta asociado a ninguna presentacion");
        }
    }
}
