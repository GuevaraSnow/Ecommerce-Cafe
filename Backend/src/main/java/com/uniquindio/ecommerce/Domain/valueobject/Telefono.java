package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.util.regex.Pattern;

public record Telefono(String telefono) {

    public Telefono {
        if (telefono == null || telefono.isBlank()) {
            throw new ReglaDominioException("El campo es obligatorio");
        }
        if (!Pattern.matches("^[0-9]{10}$", telefono)) {
            throw new ReglaDominioException("El campo debe ser de 10 digitos");
        }
    }
}