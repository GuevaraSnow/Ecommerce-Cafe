package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Mapea a: RetirarStockDePresentacion.ejecutar(...)
// Que la unidad coincida y que alcance el stock lo aplica el dominio.
public record RetirarStockRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Valid
        CantidadRequest cantidad
) {}
