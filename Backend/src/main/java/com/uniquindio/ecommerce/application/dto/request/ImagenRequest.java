package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;

// Mapea a: ImagenDePresentacion (una imagen de la Galería)
public record ImagenRequest(
        @NotBlank(message = "La url de la imagen es obligatoria")
        String url,

        boolean principal
) {}
