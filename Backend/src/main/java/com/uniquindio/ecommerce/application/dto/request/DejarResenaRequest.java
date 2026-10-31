package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Mapea a: Resena. Es el cuerpo de POST /api/resenas; el Comprador sale del token.
public record DejarResenaRequest(
        @NotBlank(message = "La Presentacion a resenar es obligatoria")
        String presentacionId,

        @NotNull(message = "La calificacion es obligatoria")
        @Min(value = 1, message = "La calificacion minima es 1")
        @Max(value = 5, message = "La calificacion maxima es 5")
        Integer calificacion,

        @NotBlank(message = "El comentario es obligatorio")
        @Size(max = 500, message = "El comentario no puede superar 500 caracteres")
        String texto
) {}
