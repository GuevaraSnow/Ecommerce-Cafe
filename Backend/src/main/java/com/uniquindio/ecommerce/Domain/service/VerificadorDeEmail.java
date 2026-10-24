package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

public class VerificadorDeEmail {

    private final UsuarioRepositorio usuarioRepositorio;

    public VerificadorDeEmail(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public void verificarEmailUnico(Email email) {
        if (usuarioRepositorio.existePorEmail(email)) {
            throw new ReglaDominioException("Ya existe un usuario registrado con el email proporcionado.");
        }
    }
}