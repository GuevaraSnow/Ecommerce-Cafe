package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.LocalDate;

/** Fecha en que se tostó el café de una Presentación; base para calcular su frescura (regla D). */
public record FechaDeTueste(LocalDate valor) {

    public FechaDeTueste {
        if (valor == null) {
            throw new ReglaDominioException("La Fecha de Tueste no puede ser nula");
        }
        if (valor.isAfter(LocalDate.now())) {
            throw new ReglaDominioException("La Fecha de Tueste no puede ser futura");
        }
    }
}
