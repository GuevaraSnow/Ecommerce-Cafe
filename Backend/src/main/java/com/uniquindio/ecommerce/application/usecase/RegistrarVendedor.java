package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Vendedor;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.VendedorRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

public class RegistrarVendedor {

    private final VendedorRepositorio repositorio;

    public RegistrarVendedor(VendedorRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Vendedor ejecutar(String nombre, Email email, Contrasena contrasena, Telefono telefono, RolVendedor rol) {
        if (repositorio.existePorEmail(email.email())) {
            throw new ReglaDominioException("Ya existe un vendedor registrado con ese email");
        }
        Vendedor vendedor = Vendedor.registrar(nombre, email, contrasena, telefono, rol);
        repositorio.guardar(vendedor);
        return vendedor;
    }
}