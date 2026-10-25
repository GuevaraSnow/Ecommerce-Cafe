package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.ContrasenaPlana;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.application.dto.request.RecuperarContrasenaRequest;
import com.uniquindio.ecommerce.application.dto.request.RestablecerContrasenaRequest;
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

    public Email toEmail(RecuperarContrasenaRequest request) {
        if (request == null || request.email() == null) {
            return null;
        }
        return new Email(request.email());
    }

    public Email toEmail(RestablecerContrasenaRequest request) {
        if (request == null || request.email() == null) {
            return null;
        }
        return new Email(request.email());
    }

    public ContrasenaPlana toContrasenaPlana(String clavePlanaTexto) {
        if (clavePlanaTexto == null || clavePlanaTexto.isBlank()) {
            return null;
        }
        return new ContrasenaPlana(clavePlanaTexto);
    }

    public Contrasena toContrasena(RestablecerContrasenaRequest request) {
        if (request == null || request.nuevaContrasena() == null) {
            return null;
        }
        return new Contrasena(request.nuevaContrasena());
    }
}