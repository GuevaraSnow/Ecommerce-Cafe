package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;

import java.util.Optional;

public interface SolicitudReembolsoRepositorio {

    Optional<SolicitudReembolso> buscarPorId(String id);

    void guardar(SolicitudReembolso solicitud);

    /**
     * Indica si ya hay una solicitud SOLICITADA o APROBADA de esa Presentación en esa Compra
     * (una rechazada no cuenta: se puede volver a pedir).
     */
    boolean existeSolicitudVigente(String compraId, String presentacionId);

    /** Indica si esa Presentación de esa Compra ya tiene su reembolso APROBADO. */
    boolean existeSolicitudAprobada(String compraId, String presentacionId);
}
