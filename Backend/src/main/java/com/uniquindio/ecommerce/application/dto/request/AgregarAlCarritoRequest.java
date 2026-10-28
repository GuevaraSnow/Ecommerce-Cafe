package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Mapea a: AgregarAlCarrito (presentacionId + Cantidad). El comprador sale del token, no del cuerpo.
public record AgregarAlCarritoRequest(
        @NotBlank(message = "La Presentacion a agregar es obligatoria")
        String presentacionId,

        @NotNull(message = "La cantidad es obligatoria")
        @Valid
        CantidadRequest cantidad
) {}
