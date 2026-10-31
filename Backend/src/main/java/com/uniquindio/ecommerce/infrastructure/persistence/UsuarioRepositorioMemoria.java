package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Reemplazado por UsuarioRepositorioJpa; con los dos activos Spring lanza NoUniqueBeanDefinitionException
//@Repository
public class UsuarioRepositorioMemoria implements UsuarioRepositorio {

    private final Map<String, Usuario> usuarios = new HashMap<>();

    @Override
    public Usuario guardar(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        if (email == null) {
            return Optional.empty();
        }
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public boolean existePorEmail(Email email) {
        if (email == null) {
            return false;
        }
        return usuarios.values().stream()
                .anyMatch(u -> u.getEmail().equals(email));
    }
}