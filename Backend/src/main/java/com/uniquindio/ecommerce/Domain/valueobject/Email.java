package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.util.regex.Pattern;

public record Email(String email) {

    public Email {
        if (email == null || email.isBlank()) {
            throw new ReglaDominioException("El campo es obligatorio");
        }

        if (!Pattern.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$", email)) {
            throw new ReglaDominioException("Ingrese un email valido");
        }
    }
}