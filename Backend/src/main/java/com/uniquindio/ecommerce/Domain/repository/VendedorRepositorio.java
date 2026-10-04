package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Vendedor;

import java.util.Optional;

public interface VendedorRepositorio {
    boolean existePorEmail(String email);
    Vendedor guardar(Vendedor vendedor);
}