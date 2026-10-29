package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Mapea a: RespuestaAResena. Es el cuerpo de PUT /api/resenas/{id}/respuesta; el vendedor sale del token.
public record ResponderResenaRequest(
        @NotBlank(message = "La respuesta es obligatoria")
        @Size(max = 500, message = "La respuesta no puede superar 500 caracteres")
        String texto
) {}
