package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/** Una imagen dentro de la Galería de una Presentación. */
public record ImagenDeProducto(String url, boolean principal) {

    public ImagenDeProducto {
        if (url == null || url.isBlank()) {
            throw new ReglaDominioException("La Imagen de Producto debe tener una url");
        }
    }
}
