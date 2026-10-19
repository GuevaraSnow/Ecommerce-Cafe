package com.uniquindio.ecommerce.infrastructure.rest.exception;

import java.time.LocalDateTime;

/** Cuerpo uniforme de toda respuesta de error de la API. */
public record ErrorResponse(LocalDateTime timestamp, int status, String mensaje) {
}
