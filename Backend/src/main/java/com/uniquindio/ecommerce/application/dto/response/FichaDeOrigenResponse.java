package com.uniquindio.ecommerce.application.dto.response;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;

import java.time.LocalDate;
import java.util.List;

public record FichaDeOrigenResponse(
        String fincaNombre,
        String cosechaInfo,
        List<LoteResumenResponse> lotesOrigen,
        String transformacionId,
        String tostadorId,
        LocalDate fechaTransformacion,
        PerfilTueste perfilTuesteResultante
) {
}