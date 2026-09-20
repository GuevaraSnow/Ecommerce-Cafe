package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.Year;

public record Cosecha(int anio, TemporadaDeCosecha temporada) {

    public Cosecha {
        int anioActual = Year.now().getValue();
        if (anio < anioActual || anio > anioActual) {
            throw new ReglaDominioException("El año de la Cosecha no es válido: " + anio);
        }
        if (temporada == null) {
            throw new ReglaDominioException("La Cosecha debe tener una temporada.");
        }
    }

    @Override
    public String toString() {
        return "Cosecha{" +
                "anio=" + anio +
                ", temporada=" + temporada +
                '}';
    }
}
