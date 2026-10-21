package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Mapea a: Precio
public record PrecioRequest(
        @NotNull(message = "El monto del precio es obligatorio")
        @Positive(message = "El monto del precio debe ser mayor que cero")
        Double monto,

        @NotBlank(message = "La moneda del precio es obligatoria")
        String moneda
) {}
