package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UbicacionRequest(
        @NotBlank(message = "El departamento es obligatorio")
        String departamento,

        @NotBlank(message = "El municipio es obligatorio")
        String municipio
) {
}