package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/** Duración en minutos de una Lección. */
public record Duracion(int minutos) {

    public Duracion {
        if (minutos <= 0) {
            throw new ReglaDominioException("La Duración debe ser mayor a cero minutos");
        }
    }
}
