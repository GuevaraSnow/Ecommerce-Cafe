package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IniciarSesion {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EncriptadorContrasenaRepositorio encriptadorContrasenaRepositorio;

    @Transactional(readOnly = true)
    public Usuario ejecutar(Email email, Contrasena contrasena) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email)
                .orElseThrow();

        if (usuario.isEliminado()) {
            throw new ReglaDominioException("El usuario se encuentra dado de baja.");
        }

        if (!encriptadorContrasenaRepositorio.coinciden(contrasena.contrasena(), usuario.getContrasena().contrasena())) {
            throw new ReglaDominioException("Credenciales inválidas.");
        }

        return usuario;
    }
}