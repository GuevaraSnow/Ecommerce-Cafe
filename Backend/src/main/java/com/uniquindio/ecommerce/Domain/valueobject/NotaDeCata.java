package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.util.List;

/** Descriptores sensoriales (notas de cata) registrados para una Presentación. */
public record NotaDeCata(List<String> descriptores) {

    public NotaDeCata {
        if (descriptores == null || descriptores.isEmpty()) {
            throw new ReglaDominioException("La Nota de Cata debe tener al menos un descriptor");
        }
        if (descriptores.stream().anyMatch(d -> d == null || d.isBlank())) {
            throw new ReglaDominioException("La Nota de Cata no admite descriptores vacíos");
        }
        descriptores = List.copyOf(descriptores);
    }
}
