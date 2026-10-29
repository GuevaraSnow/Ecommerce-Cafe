package com.uniquindio.ecommerce.application.dto.response;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;

import java.time.LocalDateTime;

/** Solicitud de Reembolso de una Presentación; fechaResolucion es null mientras siga SOLICITADO. */
public record ReembolsoResponse(
        String id,
        String compraId,
        String presentacionId,
        String vendedorId,
        String motivo,
        EstadoDeReembolso estado,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaResolucion
) {}
