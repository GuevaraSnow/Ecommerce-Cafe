package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/**
 * Estrellas que un Comprador le da a una Presentación en su Reseña: un entero de 1 a 5.
 */
public record Calificacion(int estrellas) {

    public static final int MINIMO = 1;
    public static final int MAXIMO = 5;

    /**
     * @throws ReglaDominioException si las estrellas están por fuera del rango de 1 a 5
     */
    public Calificacion {
        if (estrellas < MINIMO || estrellas > MAXIMO) {
            throw new ReglaDominioException("La calificacion debe estar entre " + MINIMO + " y " + MAXIMO
                    + " estrellas: " + estrellas);
        }
    }
}
