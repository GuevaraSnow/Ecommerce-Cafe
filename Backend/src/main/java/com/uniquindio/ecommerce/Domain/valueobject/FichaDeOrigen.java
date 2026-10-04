package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;

import java.util.List;

/**
 * Record DTO / VO de lectura que consolida la trazabilidad del café:
 * Finca + Cosecha + Lote + Transformación.
 */
public record FichaDeOrigen(
        String fincaNombre,
        String cosechaInfo,
        List lotesOrigen,
        Transformacion transformacion
) {
}