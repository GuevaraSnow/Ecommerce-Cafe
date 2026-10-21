package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Mapea a: ReponerStockDePresentacion.ejecutar(...)
// Que la unidad coincida con la de la Presentación y que el Origen tenga stock lo aplica el dominio.
public record ReponerStockRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Valid
        CantidadRequest cantidad
) {}
