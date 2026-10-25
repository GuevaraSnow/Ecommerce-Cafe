package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.application.dto.request.RestablecerContrasenaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestablecerContrasena {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public void ejecutar(RestablecerContrasenaRequest request) {
        Email email = new Email(request.email());

        Usuario usuario = usuarioRepositorio.buscarPorEmail(email).orElseThrow();

        Contrasena nuevaContrasena = new Contrasena(request.nuevaContrasena());

        usuario.cambiarContrasena(request.codigoRecuperacion(), nuevaContrasena);

        usuarioRepositorio.guardar(usuario);
    }
}