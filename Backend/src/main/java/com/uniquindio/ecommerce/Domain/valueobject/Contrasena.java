package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record Contrasena(String contrasena) {

    public Contrasena {
        if (contrasena == null || contrasena.isBlank()) {
            throw new ReglaDominioException("El campo es obligatorio");
        }
    }
}