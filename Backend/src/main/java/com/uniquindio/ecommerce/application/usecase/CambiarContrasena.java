package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeContrasena;
import com.uniquindio.ecommerce.Domain.valueobject.ContrasenaPlana;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CambiarContrasena {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ServicioDeContrasena servicioDeContrasena;

    @Transactional
    public void ejecutar(String usuarioId, String contrasenaActualTexto, ContrasenaPlana nuevaContrasenaPlana) {
        Usuario usuario = usuarioRepositorio.buscarPorId(usuarioId).orElseThrow();

        servicioDeContrasena.cambiarContrasena(usuario, contrasenaActualTexto, nuevaContrasenaPlana);

        usuarioRepositorio.guardar(usuario);
    }
}