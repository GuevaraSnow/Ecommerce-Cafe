package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompradorRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

public class RegistrarComprador {

    private final CompradorRepositorio compradorRepositorio;

    public RegistrarComprador(CompradorRepositorio compradorRepositorio) {
        this.compradorRepositorio = compradorRepositorio;
    }

    public Comprador ejecutar(String id, String nombre, Email email, Contrasena contrasena,
                              Telefono telefono, FechaDeNacimiento fechaNacimiento, TipoDeComprador tipo) {

        // Regla de email único
        if (compradorRepositorio.existePorEmail(email)) {
            throw new ReglaDominioException("El email ya se encuentra registrado.");
        }

        Comprador comprador = new Comprador(id, nombre, email, contrasena, telefono, fechaNacimiento, tipo);
        return compradorRepositorio.guardar(comprador);
    }
}