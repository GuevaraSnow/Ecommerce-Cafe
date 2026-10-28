package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.CompraPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CompraRepositorioJpa implements CompraRepositorio {

    /** Una Compra está activa mientras siga en curso (igual que Compra.estaActiva()). */
    private static final EnumSet<EstadoDeCompra> ACTIVAS =
            EnumSet.of(EstadoDeCompra.PENDIENTE, EstadoDeCompra.CONFIRMADA, EstadoDeCompra.ENVIADA);

    private final CompraJpaDataRepository dataRepository;
    private final CompraPersistenceMapper mapper;

    @Override
    public Optional<Compra> buscarPorId(String id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void guardar(Compra compra) {
        dataRepository.save(mapper.toEntity(compra));
    }

    @Override
    public boolean existeCompraActivaConPresentacion(String presentacionId) {
        return dataRepository.existsByEstadoInAndDetalles_PresentacionId(ACTIVAS, presentacionId);
    }

    @Override
    public boolean existeCompraEntregada(String compradorId, String presentacionId) {
        return dataRepository.existsByCompradorIdAndEstadoAndDetalles_PresentacionId(
                compradorId, EstadoDeCompra.ENTREGADA, presentacionId);
    }
}
