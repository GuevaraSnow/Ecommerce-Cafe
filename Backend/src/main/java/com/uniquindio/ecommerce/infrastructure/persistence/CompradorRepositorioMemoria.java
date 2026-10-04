package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.repository.CompradorRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompradorRepositorioMemoria implements CompradorRepositorio {
    private final Map<String, Comprador> compradores = new HashMap<>();

    @Override
    public boolean existePorEmail(Email email) {
        if (email == null) return false;

        for (Comprador comprador : compradores.values()) {
            if (comprador.getEmail() != null && comprador.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Comprador> buscarPorEmail(Email email) {
        if (email == null) return Optional.empty();

        for (Comprador comprador : compradores.values()) {
            if (comprador.getEmail() != null && comprador.getEmail().equals(email)) {
                return Optional.of(comprador);
            }
        }
        return Optional.empty();
    }

    @Override
    public Comprador guardar(Comprador comprador) {
        compradores.put(comprador.getId(), comprador);
        return comprador;
    }
}