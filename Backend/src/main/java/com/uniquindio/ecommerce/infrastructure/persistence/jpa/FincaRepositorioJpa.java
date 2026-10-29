package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.FincaPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class FincaRepositorioJpa implements FincaRepositorio {

    private final FincaJpaDataRepository dataRepository;
    private final FincaPersistenceMapper mapper;

    @Override
    public void guardar(Finca finca) {
        dataRepository.save(mapper.toEntity(finca));
    }

    @Override
    public Optional<Finca> buscarPorId(String id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }
}