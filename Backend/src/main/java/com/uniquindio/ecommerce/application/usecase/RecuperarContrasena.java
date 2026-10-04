package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompradorRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeRecuperacion;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.Optional;

public class RecuperarContrasena {

    private final CompradorRepositorio compradorRepositorio;

    public RecuperarContrasena(CompradorRepositorio compradorRepositorio) {
        this.compradorRepositorio = compradorRepositorio;
    }

    public CodigoDeRecuperacion ejecutar(Email email) {
        Optional compradorOpt = compradorRepositorio.buscarPorEmail(email);

        if (compradorOpt == null || compradorOpt.isEmpty()) {
            throw new ReglaDominioException("No existe un usuario asociado al email ingresado.");
        }

        Comprador comprador = (Comprador) compradorOpt.get();

        CodigoDeRecuperacion codigo = CodigoDeRecuperacion.generar();
        comprador.asignarCodigoRecuperacion(codigo);
        compradorRepositorio.guardar(comprador);

        return codigo;
    }
}