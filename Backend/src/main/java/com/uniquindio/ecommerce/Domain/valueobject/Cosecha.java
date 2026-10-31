package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.Year;

/**
 * Año y temporada en que se recolectó el café de un Lote. Se acepta la cosecha del
 * año en curso o la del anterior (el café de la traviesa de un año suele registrarse
 * al comenzar el siguiente); nunca una cosecha futura.
 */
public record Cosecha(int anio, TemporadaDeCosecha temporada) {

    public Cosecha {
        int anioActual = Year.now().getValue();
        if (anio > anioActual) {
            throw new ReglaDominioException("La Cosecha no puede ser de un año futuro: " + anio);
        }
        if (temporada == null) {
            throw new ReglaDominioException("La Cosecha debe tener una temporada.");
        }
    }

    public boolean esRegistrable(int anioDeRegistro) {
        return anio >= anioDeRegistro - 1;
    }

    @Override
    public String toString() {
        return "Cosecha{" +
                "anio=" + anio +
                ", temporada=" + temporada +
                '}';
    }
}
