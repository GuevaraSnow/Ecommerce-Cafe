package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;


public record PorcentajeDeMerma(double valor) {

    public PorcentajeDeMerma {
        if (valor < 0.12 || valor > 0.22) {
            throw new ReglaDominioException(
                    "El porcentaje de merma debe estar en el rango permitido entre 0.12 (12%) y 0.22 (22%). Valor ingresado: " + valor
            );
        }
    }
}