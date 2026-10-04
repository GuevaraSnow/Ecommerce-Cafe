package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record DetalleDeCompra(String compraId, String presentacionId, Precio precioCongelado, Cantidad cantidad) {

    public DetalleDeCompra {
        if (compraId == null || compraId.isBlank()) {
            throw new ReglaDominioException("No esta asociado a ninguna compra");
        }
        if (presentacionId == null || presentacionId.isBlank()) {
            throw new ReglaDominioException("No esta asociado a ninguna presentacion");
        }
        if (precioCongelado == null) {
            throw new ReglaDominioException("El detalle debe tener un precio");
        }
        if (cantidad == null || cantidad.valor() <= 0) {
            throw new ReglaDominioException("El detalle debe tener una cantidad mayor a cero");
        }
    }
}