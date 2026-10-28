package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.Optional;

public interface UsuarioRepositorio {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorId(String id);
    Optional<Usuario> buscarPorEmail(Email email);
    boolean existePorEmail(Email email);
}