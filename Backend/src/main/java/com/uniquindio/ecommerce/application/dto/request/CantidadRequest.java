package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Mapea a: Cantidad
public record CantidadRequest(
        @NotNull(message = "El valor de la cantidad es obligatorio")
        Double valor,

        @NotBlank(message = "La unidad de la cantidad es obligatoria")
        String unidad
) {}
