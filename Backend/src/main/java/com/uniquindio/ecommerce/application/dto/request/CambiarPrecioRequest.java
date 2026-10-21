package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Mapea a: CambiarPrecioDePresentacion.ejecutar(...)
public record CambiarPrecioRequest(
        @NotNull(message = "El precio es obligatorio")
        @Valid
        PrecioRequest precio
) {}
