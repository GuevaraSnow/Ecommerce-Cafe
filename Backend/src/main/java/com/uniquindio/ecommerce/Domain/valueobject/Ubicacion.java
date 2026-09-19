package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record Ubicacion(String departamento,String municipio) {

    public Ubicacion {
        if ( departamento == null || departamento.isEmpty() || departamento.isEmpty() ) {
            throw new ReglaDominioException("El departamento es obligatorio");
        }
        if (municipio == null || municipio.isEmpty() || municipio.isBlank()) {
            throw new ReglaDominioException("El municipio es obligatorio");
        }
    }
}

