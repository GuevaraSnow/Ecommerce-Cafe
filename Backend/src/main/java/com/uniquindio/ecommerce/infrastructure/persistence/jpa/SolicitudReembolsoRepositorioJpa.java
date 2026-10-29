package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.SolicitudReembolsoPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SolicitudReembolsoRepositorioJpa implements SolicitudReembolsoRepositorio {

    private final SolicitudReembolsoJpaDataRepository dataRepository;
    private final SolicitudReembolsoPersistenceMapper mapper;

    @Override
    public Optional<SolicitudReembolso> buscarPorId(String id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void guardar(SolicitudReembolso solicitud) {
        dataRepository.save(mapper.toEntity(solicitud));
    }

    @Override
    public boolean existeSolicitudVigente(String compraId, String presentacionId) {
        return dataRepository.existsByCompraIdAndPresentacionIdAndEstadoIn(compraId, presentacionId,
                EnumSet.of(EstadoDeReembolso.SOLICITADO, EstadoDeReembolso.APROBADO));
    }

    @Override
    public boolean existeSolicitudAprobada(String compraId, String presentacionId) {
        return dataRepository.existsByCompraIdAndPresentacionIdAndEstado(compraId, presentacionId,
                EstadoDeReembolso.APROBADO);
    }
}
