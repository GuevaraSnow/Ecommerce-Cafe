package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/**
 * Texto escrito en una Reseña: el comentario del Comprador o la respuesta del vendedor.
 * No puede estar vacío ni pasar de 500 caracteres; se guarda sin espacios sobrantes en los extremos.
 */
public record TextoDeResena(String valor) {

    public static final int LONGITUD_MAXIMA = 500;

    /**
     * @throws ReglaDominioException si el texto está vacío o supera los 500 caracteres
     */
    public TextoDeResena {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException("El texto de la resena no puede estar vacio");
        }
        valor = valor.strip();
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new ReglaDominioException("El texto de la resena no puede superar " + LONGITUD_MAXIMA
                    + " caracteres (tiene " + valor.length() + ")");
        }
    }
}
