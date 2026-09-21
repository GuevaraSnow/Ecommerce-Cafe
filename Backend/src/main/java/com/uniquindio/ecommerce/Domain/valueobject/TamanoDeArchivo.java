package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/** Tamaño en bytes de un Archivo Digital. */
public record TamanoDeArchivo(long bytes) {

    public TamanoDeArchivo {
        if (bytes <= 0) {
            throw new ReglaDominioException("El tamaño del Archivo Digital debe ser mayor a cero");
        }
    }
}
