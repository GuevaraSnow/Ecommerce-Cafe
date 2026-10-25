package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarFincaRequest(
        @NotBlank(message = "El nombre de la finca es obligatorio")
        String nombre,

        @NotNull(message = "La ubicación es obligatoria")
        UbicacionRequest ubicacion,

        @NotNull(message = "La altitud es obligatoria")
        Integer altitud
) {
}