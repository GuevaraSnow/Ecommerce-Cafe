package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/** Una imagen dentro de la Galería de una Presentación. */
public record ImagenDePresentacion(String url, boolean principal) {

    public ImagenDePresentacion {
        if (url == null || url.isBlank()) {
            throw new ReglaDominioException("La Imagen de Presentación debe tener una url");
        }
    }
}
