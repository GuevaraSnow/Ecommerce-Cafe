package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Compra;

import java.util.Optional;

public interface CompraRepositorio {
    Optional<Compra> buscarPorId(String id);

    void guardar(Compra compra);
}