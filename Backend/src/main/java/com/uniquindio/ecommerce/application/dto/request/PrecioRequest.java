package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Mapea a: Precio
public record PrecioRequest(
        @NotNull(message = "El monto del precio es obligatorio")
        Double monto,

        @NotBlank(message = "La moneda del precio es obligatoria")
        String moneda
) {}
