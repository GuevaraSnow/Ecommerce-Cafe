package com.uniquindio.ecommerce.application.dto.response;

public record LoteResumenResponse(
        String codigo,
        String fincaId,
        CantidadResponse cantidadDisponible,
        String procesoBeneficio,
        String estado
) {
}