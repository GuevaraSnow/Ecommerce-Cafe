package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.application.dto.request.IniciarSesionRequest;
import com.uniquindio.ecommerce.application.dto.response.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IniciarSesion {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional(readOnly = true)
    public UsuarioResponse ejecutar(IniciarSesionRequest request) {
        Email email = new Email(request.email());

        Usuario usuario = usuarioRepositorio.buscarPorEmail(email)
                .orElseThrow();
        if (!usuario.getContrasena().contrasena().equals(request.contrasena())) {
            throw new ReglaDominioException("Credenciales inválidas.");
        }

        return UsuarioResponse.desdeDominio(usuario);
    }
}