package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.SolicitudReembolsoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

interface SolicitudReembolsoJpaDataRepository extends JpaRepository<SolicitudReembolsoEntity, String> {

    boolean existsByCompraIdAndPresentacionIdAndEstadoIn(
            String compraId, String presentacionId, Collection<EstadoDeReembolso> estados);

    boolean existsByCompraIdAndPresentacionIdAndEstado(
            String compraId, String presentacionId, EstadoDeReembolso estado);
}
