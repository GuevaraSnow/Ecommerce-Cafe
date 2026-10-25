package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.service.VerificadorDeEmail;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarUsuario {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EncriptadorContrasenaRepositorio encriptadorContrasenaRepositorio;
    private final VerificadorDeEmail verificadorDeEmail;

    @Transactional
    public Usuario ejecutar(String nombre, Email email, ContrasenaPlana contrasenaPlana,
                            Telefono telefono, FechaDeNacimiento fechaNacimiento, Rol rol) {

        verificadorDeEmail.verificarEmailUnico(email);

        String id = UUID.randomUUID().toString();
        String contrasenaEncriptada = encriptadorContrasenaRepositorio.encriptar(contrasenaPlana.valor());
        Contrasena contrasena = new Contrasena(contrasenaEncriptada);

        Usuario usuario = Usuario.registrar(
                id,
                nombre,
                email,
                contrasena,
                telefono,
                fechaNacimiento,
                rol
        );

        usuarioRepositorio.guardar(usuario);
        return usuario;
    }
}