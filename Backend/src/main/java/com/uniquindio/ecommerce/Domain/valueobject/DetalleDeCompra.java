package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record DetalleDeCompra(String compraId, String presentacionId, Precio precioCongelado, Cantidad cantidad) {
    public DetalleDeCompra {
        if (compraId == null || compraId.isBlank()) {
            throw new ReglaDominioException("No esta asociado a ninguna compra");
        }
        if (presentacionId == null || presentacionId.isBlank()) {
            throw  new ReglaDominioException("No esta asociado a ninguna presentacion");
        }
    }
}
