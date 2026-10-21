package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/**
 * Calificación que un Comprador le da a una Presentación en su Reseña: un entero de 1 a 5.
 * Cómo se dibuja (tazas, granos de café…) lo decide el front.
 */
public record Calificacion(int valor) {

    public static final int MINIMO = 1;
    public static final int MAXIMO = 5;

    /**
     * @throws ReglaDominioException si el valor está por fuera del rango de 1 a 5
     */
    public Calificacion {
        if (valor < MINIMO || valor > MAXIMO) {
            throw new ReglaDominioException("La calificacion debe estar entre " + MINIMO + " y " + MAXIMO
                    + ": " + valor);
        }
    }
}
