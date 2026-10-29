package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Reemplazado por SolicitudReembolsoRepositorioJpa; con los dos activos Spring lanza NoUniqueBeanDefinitionException
//@Repository
public class SolicitudReembolsoRepositorioMemoria implements SolicitudReembolsoRepositorio {

    private final Map<String, SolicitudReembolso> solicitudes = new HashMap<>();

    @Override
    public Optional<SolicitudReembolso> buscarPorId(String id) {
        return Optional.ofNullable(solicitudes.get(id));
    }

    @Override
    public void guardar(SolicitudReembolso solicitud) {
        solicitudes.put(solicitud.getId(), solicitud);
    }

    @Override
    public boolean existeSolicitudVigente(String compraId, String presentacionId) {
        return solicitudes.values().stream()
                .anyMatch(solicitud -> solicitud.getCompraId().equals(compraId)
                        && solicitud.getPresentacionId().equals(presentacionId)
                        && solicitud.getEstado() != EstadoDeReembolso.RECHAZADO);
    }

    @Override
    public boolean existeSolicitudAprobada(String compraId, String presentacionId) {
        return solicitudes.values().stream()
                .anyMatch(solicitud -> solicitud.getCompraId().equals(compraId)
                        && solicitud.getPresentacionId().equals(presentacionId)
                        && solicitud.getEstado() == EstadoDeReembolso.APROBADO);
    }
}
