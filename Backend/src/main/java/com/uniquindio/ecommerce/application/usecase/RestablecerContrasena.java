package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompradorRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.Optional;

public class RestablecerContrasena {

    private final CompradorRepositorio compradorRepositorio;

    public RestablecerContrasena(CompradorRepositorio compradorRepositorio) {
        this.compradorRepositorio = compradorRepositorio;
    }

    public void ejecutar(Email email, String codigoIngresado, Contrasena nuevaContrasena) {
        Optional compradorOpt = compradorRepositorio.buscarPorEmail(email);

        if (compradorOpt == null || compradorOpt.isEmpty()) {
            throw new ReglaDominioException("Usuario no encontrado.");
        }

        // Casteo explícito a Comprador para evitar problemas de inferencia
        Comprador comprador = (Comprador) compradorOpt.get();

        if (comprador.getCodigoRecuperacion() == null) {
            throw new ReglaDominioException("No hay una solicitud de recuperación activa para este usuario.");
        }

        if (!comprador.getCodigoRecuperacion().codigo().equals(codigoIngresado)) {
            throw new ReglaDominioException("El código ingresado es incorrecto.");
        }

        if (!comprador.getCodigoRecuperacion().estaVigente()) {
            throw new ReglaDominioException("El código de recuperación ha expirado.");
        }

        comprador.cambiarContrasena(nuevaContrasena);
        compradorRepositorio.guardar(comprador);
    }
}