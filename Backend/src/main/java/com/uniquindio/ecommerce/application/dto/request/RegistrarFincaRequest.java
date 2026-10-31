package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegistrarFincaRequest(
        @NotBlank(message = "El nombre de la finca es obligatorio")
        String nombre,

        @NotNull(message = "La ubicación es obligatoria")
        @Valid
        UbicacionRequest ubicacion,

        @NotNull(message = "La altitud es obligatoria")
        @Positive(message = "La altitud debe ser mayor que cero")
        Integer altitud
) {
}