package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.CredencialesInvalidasException;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

/**
 * Domain Service que autentica a un Usuario: busca por correo y compara la contraseña con la guardada
 * (encriptada). Es una regla que cruza al Usuario y al encriptador, y por eso no vive en el caso de uso.
 * Un correo que no existe, una contraseña que no coincide y un usuario dado de baja fallan igual, con la
 * misma excepción y el mismo mensaje, para que quien intenta entrar no pueda saber cuál fue la causa.
 */
public class ServicioDeAutenticacion {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EncriptadorContrasenaRepositorio encriptador;

    public ServicioDeAutenticacion(UsuarioRepositorio usuarioRepositorio, EncriptadorContrasenaRepositorio encriptador) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.encriptador = encriptador;
    }

    /**
     * @throws CredencialesInvalidasException si el correo no existe, la contraseña no coincide o el usuario
     *                                        está dado de baja
     */
    public Usuario autenticar(Email email, Contrasena contrasena) {
        return usuarioRepositorio.buscarPorEmail(email)
                .filter(usuario -> !usuario.isEliminado())
                .filter(usuario -> encriptador.coinciden(contrasena.contrasena(), usuario.getContrasena().contrasena()))
                .orElseThrow(CredencialesInvalidasException::new);
    }
}
