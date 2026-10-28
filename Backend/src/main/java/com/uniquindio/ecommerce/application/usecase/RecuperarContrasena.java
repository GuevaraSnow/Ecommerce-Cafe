package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeRecuperacion;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.application.dto.request.RecuperarContrasenaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecuperarContrasena {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public String ejecutar(RecuperarContrasenaRequest request) {
        Email email = new Email(request.email());

        Usuario usuario = usuarioRepositorio.buscarPorEmail(email).orElseThrow();

        CodigoDeRecuperacion codigo = CodigoDeRecuperacion.generar();
        usuario.asignarCodigoRecuperacion(codigo);

        usuarioRepositorio.guardar(usuario);

        return codigo.codigo();
    }
}