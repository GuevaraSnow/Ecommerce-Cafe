package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.application.dto.response.ReembolsoResponse;
import org.springframework.stereotype.Component;

/** Traduce una Solicitud de Reembolso del dominio a su DTO de respuesta. */
@Component
public class ReembolsoMapper {

    public ReembolsoResponse toResponse(SolicitudReembolso solicitud) {
        return new ReembolsoResponse(
                solicitud.getId(),
                solicitud.getCompraId(),
                solicitud.getPresentacionId(),
                solicitud.getVendedorId(),
                solicitud.getMotivo(),
                solicitud.getEstado(),
                solicitud.getFechaSolicitud(),
                solicitud.getFechaResolucion().orElse(null));
    }
}
