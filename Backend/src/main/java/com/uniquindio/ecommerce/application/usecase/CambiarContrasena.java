package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.application.dto.request.CambiarContrasenaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CambiarContrasena {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EncriptadorContrasenaRepositorio encriptadorDeContrasena;

    @Transactional
    public void ejecutar(String usuarioId, CambiarContrasenaRequest request) {
        Usuario usuario = usuarioRepositorio.buscarPorId(usuarioId).orElseThrow();

        Contrasena contrasenaActual = new Contrasena(request.contrasenaActual());
        Contrasena nuevaContrasena = new Contrasena(request.nuevaContrasena());

        usuario.cambiarContrasenaConVerificacion(contrasenaActual, nuevaContrasena, encriptadorDeContrasena);

        usuarioRepositorio.guardar(usuario);
    }
}