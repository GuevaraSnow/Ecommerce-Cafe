package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.application.dto.response.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().email(),
                usuario.getTelefono() != null ? usuario.getTelefono().telefono() : null,
                usuario.getRol().name()
        );
    }
}