package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.repository.CompradorRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompradorRepositorioMemoria implements CompradorRepositorio {
    private final Map compradores = new HashMap<>();

    @Override
    public boolean existePorEmail(Email email) {
        if (email == null) return false;
        return compradores.values().stream()
                .anyMatch(c -> c.getEmail() != null && c.getEmail().equals(email));
    }

    @Override
    public Optional buscarPorEmail(Email email) {
        if (email == null) return Optional.empty();
        return compradores.values().stream()
                .filter(c -> c.getEmail() != null && c.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Comprador guardar(Comprador comprador) {
        compradores.put(comprador.getId(), comprador);
        return comprador;
    }
}

