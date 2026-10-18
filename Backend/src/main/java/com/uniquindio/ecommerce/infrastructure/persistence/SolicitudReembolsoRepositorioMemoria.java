package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
}
