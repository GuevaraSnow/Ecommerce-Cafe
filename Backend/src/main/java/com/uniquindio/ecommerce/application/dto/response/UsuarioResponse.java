package com.uniquindio.ecommerce.application.dto.response;

import com.uniquindio.ecommerce.Domain.entity.Usuario;

public record UsuarioResponse(
        String id,
        String nombre,
        String email,
        String telefono,
        String rol
) {
    public static UsuarioResponse desdeDominio(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().email(),
                usuario.getTelefono() != null ? usuario.getTelefono().telefono() : null,
                usuario.getRol().name()
        );
    }
}