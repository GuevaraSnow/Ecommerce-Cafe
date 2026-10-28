package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.SolicitudReembolsoEntity;
import org.springframework.stereotype.Component;

/** Convierte SolicitudReembolso (dominio) en SolicitudReembolsoEntity (tabla) y de vuelta. */
@Component
public class SolicitudReembolsoPersistenceMapper {

    public SolicitudReembolsoEntity toEntity(SolicitudReembolso solicitud) {
        SolicitudReembolsoEntity entity = new SolicitudReembolsoEntity();
        entity.setId(solicitud.getId());
        entity.setCompraId(solicitud.getCompraId());
        entity.setPresentacionId(solicitud.getPresentacionId());
        entity.setVendedorId(solicitud.getVendedorId());
        entity.setMotivo(solicitud.getMotivo());
        entity.setFechaSolicitud(solicitud.getFechaSolicitud());
        entity.setEstado(solicitud.getEstado());
        entity.setFechaResolucion(solicitud.getFechaResolucion().orElse(null));
        return entity;
    }

    public SolicitudReembolso toDomain(SolicitudReembolsoEntity entity) {
        return SolicitudReembolso.reconstruir(
                entity.getId(),
                entity.getCompraId(),
                entity.getPresentacionId(),
                entity.getVendedorId(),
                entity.getMotivo(),
                entity.getFechaSolicitud(),
                entity.getEstado(),
                entity.getFechaResolucion());
    }
}
