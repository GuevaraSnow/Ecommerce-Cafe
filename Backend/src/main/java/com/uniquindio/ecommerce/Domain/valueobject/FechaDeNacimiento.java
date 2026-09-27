package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import java.time.LocalDate;
import java.time.Period;

public record FechaDeNacimiento(LocalDate valor) {

    public FechaDeNacimiento {
        if (valor == null) {
            throw new ReglaDominioException("La fecha de nacimiento es obligatoria");
        }
        if (valor.isAfter(LocalDate.now())) {
            throw new ReglaDominioException("La fecha de nacimiento no puede ser futura");
        }
    }

    public boolean esMayorDeEdad() {
        return Period.between(valor, LocalDate.now()).getYears() >= 18;
    }
}