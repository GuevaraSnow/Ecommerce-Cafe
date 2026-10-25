package com.uniquindio.ecommerce.application.dto.response;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;

import java.time.LocalDate;

public record TransformacionResponse(
        String id,
        String loteCodigo,
        CantidadResponse cantidadEntrada,
        Double merma,
        CantidadResponse cantidadResultante,
        CantidadResponse cantidadResultanteDisponible,
        String tostadorId,
        LocalDate fecha,
        PerfilTueste perfilTuesteResultante
) {
}