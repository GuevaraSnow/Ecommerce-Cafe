package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.UsuarioPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositorioJpa implements UsuarioRepositorio {

    private final UsuarioJpaDataRepository dataRepository;
    private final UsuarioPersistenceMapper mapper;

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity savedEntity = dataRepository.save(mapper.toEntity(usuario));
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        if (email == null) return Optional.empty();
        return dataRepository.findByEmail(email.email()).map(mapper::toDomain);
    }

    @Override
    public boolean existePorEmail(Email email) {
        if (email == null) return false;
        return dataRepository.existsByEmail(email.email());
    }
}