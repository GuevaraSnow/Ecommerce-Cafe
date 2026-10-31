package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.entity.Lote;

import java.time.LocalDate;
import java.util.List;

/**
 * Record DTO / VO de lectura que consolida la trazabilidad del café
 * con datos puros, sin acoplar la entidad Transformacion.
 */
public record FichaDeOrigen(
        String fincaNombre,
        Cosecha cosecha,
        List<Lote> lotesOrigen,
        String transformacionId,
        String tostadorId,
        LocalDate fechaTransformacion,
        PerfilTueste perfilTuesteResultante
) {
}