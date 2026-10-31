package com.uniquindio.ecommerce.application.dto.response;

import java.time.LocalDateTime;

/** Reseña de una Presentación; la calificación es un entero de 1 a 5 y respuesta es null si el vendedor no ha respondido. */
public record ResenaResponse(
        String id,
        String compradorId,
        String presentacionId,
        String vendedorId,
        int calificacion,
        String texto,
        LocalDateTime fecha,
        RespuestaAResenaResponse respuesta
) {}
