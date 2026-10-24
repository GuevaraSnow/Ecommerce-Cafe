package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeNacimiento;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import com.uniquindio.ecommerce.application.dto.request.RegistrarUsuarioRequest;
import com.uniquindio.ecommerce.application.dto.response.UsuarioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarUsuario {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EncriptadorContrasenaRepositorio encriptadorContrasenaRepositorio;

    @Transactional
    public UsuarioResponse ejecutar(RegistrarUsuarioRequest request) {
        Email email = new Email(request.email());

        if (usuarioRepositorio.existePorEmail(email)) {
            throw new ReglaDominioException("Ya existe un usuario registrado con el email proporcionado.");
        }

        String id = UUID.randomUUID().toString();
        String contrasenaEncriptada = encriptadorContrasenaRepositorio.encriptar(request.contrasena());
        Contrasena contrasena = new Contrasena(contrasenaEncriptada);
        Telefono telefono = request.telefono() != null && !request.telefono().isBlank()
                ? new Telefono(request.telefono()) : null;
        FechaDeNacimiento fechaNacimiento = request.fechaDeNacimiento() != null
                ? new FechaDeNacimiento(request.fechaDeNacimiento()) : null;

        Usuario usuario = Usuario.registrar(
                id,
                request.nombre(),
                email,
                contrasena,
                telefono,
                fechaNacimiento,
                request.rol()
        );

        usuarioRepositorio.guardar(usuario);
        return UsuarioResponse.desdeDominio(usuario);
    }
}