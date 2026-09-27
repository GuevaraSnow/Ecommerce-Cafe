package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record DireccionDeEnvio(String ciudad, String direccion, String destinatario) {

    public DireccionDeEnvio {
        if (ciudad == null || ciudad.isBlank()) {
            throw new ReglaDominioException("El campo no puede estar vacio");
        }
        if (direccion == null || direccion.isBlank()) {
            throw new ReglaDominioException("El campo no puede estar vacio");
        }
        if (destinatario == null || destinatario.isBlank()) {
            throw new ReglaDominioException("El campo no puede estar vacio");
        }
    }
}