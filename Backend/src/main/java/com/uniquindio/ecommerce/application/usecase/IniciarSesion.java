package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompradorRepositorio;
import com.uniquindio.ecommerce.Domain.repository.VendedorRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.Optional;

public class IniciarSesion {

    private final CompradorRepositorio compradorRepositorio;
    private final VendedorRepositorio vendedorRepositorio;

    public IniciarSesion(CompradorRepositorio compradorRepositorio, VendedorRepositorio vendedorRepositorio) {
        this.compradorRepositorio = compradorRepositorio;
        this.vendedorRepositorio = vendedorRepositorio;
    }

    public Object ejecutar(Email email, Contrasena contrasena) {
        Optional compradorOpt = compradorRepositorio.buscarPorEmail(email);

        if (compradorOpt != null && compradorOpt.isPresent()) {
            Comprador comprador = (Comprador) compradorOpt.get();

            if (comprador.getContraseña().equals(contrasena)) {
                return comprador;
            }
            throw new ReglaDominioException("Contraseña incorrecta.");
        }

        throw new ReglaDominioException("No existe un usuario registrado con el email especificado.");
    }
}