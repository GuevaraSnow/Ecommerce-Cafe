package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record VariedadDeCafe(String nombre) {

    public VariedadDeCafe {
        if (nombre == null) {
            throw new ReglaDominioException("La Variedad de Café no puede estar vacía.");
        }
    }


}


