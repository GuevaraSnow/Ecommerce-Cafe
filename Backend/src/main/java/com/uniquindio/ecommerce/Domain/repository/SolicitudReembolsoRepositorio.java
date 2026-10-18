package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;

import java.util.Optional;

public interface SolicitudReembolsoRepositorio {

    Optional<SolicitudReembolso> buscarPorId(String id);

    void guardar(SolicitudReembolso solicitud);
}
