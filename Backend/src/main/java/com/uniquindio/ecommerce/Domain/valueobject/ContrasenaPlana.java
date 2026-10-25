package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record ContrasenaPlana(String valor) {

    public ContrasenaPlana {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("La contraseña no puede estar vacía.");
        }
        if (valor.length() < 8) {
            throw new ReglaDominioException("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!valor.matches(".*[A-Z].*")) {
            throw new ReglaDominioException("La contraseña debe contener al menos una letra mayúscula.");
        }
        if (!valor.matches(".*[0-9].*")) {
            throw new ReglaDominioException("La contraseña debe contener al menos un número.");
        }
    }
}