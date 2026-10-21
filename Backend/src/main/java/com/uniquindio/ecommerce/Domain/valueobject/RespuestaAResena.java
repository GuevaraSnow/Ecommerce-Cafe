package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Respuesta que el vendedor dueño de la Presentación deja en una Reseña: el texto y cuándo
 * lo escribió. No tiene identidad propia; vive dentro de la Reseña.
 */
public record RespuestaAResena(TextoDeResena texto, LocalDateTime fecha) {

    /**
     * @throws ReglaDominioException si falta el texto o la fecha
     */
    public RespuestaAResena {
        if (texto == null) {
            throw new ReglaDominioException("La respuesta debe tener un texto");
        }
        if (fecha == null) {
            throw new ReglaDominioException("La respuesta debe registrar la fecha en que se escribio");
        }
    }
}
