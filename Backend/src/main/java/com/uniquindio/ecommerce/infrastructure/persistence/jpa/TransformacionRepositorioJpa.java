package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.TransformacionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class TransformacionRepositorioJpa implements TransformacionRepositorio {

    private final TransformacionJpaDataRepository dataRepository;
    private final TransformacionPersistenceMapper mapper;

    @Override
    public void guardar(Transformacion transformacion) {
        dataRepository.save(mapper.toEntity(transformacion));
    }

    @Override
    public Optional<Transformacion> buscarPorId(String id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }
}