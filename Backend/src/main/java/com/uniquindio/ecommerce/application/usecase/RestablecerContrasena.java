package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestablecerContrasena {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public void ejecutar(Email email, String codigoTexto, Contrasena nuevaContrasena) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email).orElseThrow();

        usuario.cambiarContrasena(codigoTexto, nuevaContrasena);

        usuarioRepositorio.guardar(usuario);
    }
}