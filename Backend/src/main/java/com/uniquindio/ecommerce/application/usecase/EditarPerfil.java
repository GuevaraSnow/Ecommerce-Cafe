package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: EditarPerfil.
 * (Actor: cualquier usuario autenticado) — cambia el nombre y el teléfono del propio usuario (sin teléfono, se
 * quita). No tiene lógica: las reglas del nombre las aplica el Usuario.
 */
@Service
@RequiredArgsConstructor
public class EditarPerfil {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public Usuario ejecutar(String usuarioId, String nombre, Telefono telefono) {
        Usuario usuario = usuarioRepositorio.buscarPorId(usuarioId).orElseThrow();
        usuario.editarPerfil(nombre, telefono);
        usuarioRepositorio.guardar(usuario);
        return usuario;
    }
}
