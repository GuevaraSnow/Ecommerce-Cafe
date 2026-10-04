package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompraRepositorioMemoria implements CompraRepositorio {

    private final Map<String, Compra> compras = new HashMap<>();

    @Override
    public Optional<Compra> buscarPorId(String id) {
        return Optional.ofNullable(compras.get(id));
    }

    @Override
    public void guardar(Compra compra) {
        compras.put(compra.getId(), compra);
    }
}