package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import com.uniquindio.ecommerce.application.dto.request.EditarPerfilRequest;
import com.uniquindio.ecommerce.application.dto.response.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditarPerfil {

    private final UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public UsuarioResponse ejecutar(String usuarioId, EditarPerfilRequest request) {
        Usuario usuario = usuarioRepositorio.buscarPorId(usuarioId).orElseThrow();

        Telefono nuevoTelefono = request.telefono() != null && !request.telefono().isBlank()
                ? new Telefono(request.telefono()) : null;

        usuario.editarPerfil(request.nombre(), nuevoTelefono);

        usuarioRepositorio.guardar(usuario);
        return UsuarioResponse.desdeDominio(usuario);
    }
}