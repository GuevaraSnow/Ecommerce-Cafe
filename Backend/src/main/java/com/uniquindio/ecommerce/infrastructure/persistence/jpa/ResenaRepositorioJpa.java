package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.ResenaPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ResenaRepositorioJpa implements ResenaRepositorio {

    private final ResenaJpaDataRepository dataRepository;
    private final ResenaPersistenceMapper mapper;

    @Override
    public Optional<Resena> buscarPorId(String id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void guardar(Resena resena) {
        dataRepository.save(mapper.toEntity(resena));
    }

    @Override
    public List<Resena> buscarPorPresentacion(String presentacionId) {
        return dataRepository.findByPresentacionIdOrderByFechaDesc(presentacionId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existeResenaDe(String compradorId, String presentacionId) {
        return dataRepository.existsByCompradorIdAndPresentacionId(compradorId, presentacionId);
    }
}
