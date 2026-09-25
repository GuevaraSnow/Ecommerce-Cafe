package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Vendedor;
import com.uniquindio.ecommerce.Domain.repository.VendedorRepositorio;

import java.util.HashMap;
import java.util.Map;

public class VendedorRepositorioMemoria implements VendedorRepositorio {

    private final Map<String, Vendedor> vendedores = new HashMap<>();

    @Override
    public boolean existePorEmail(String email) {
        return vendedores.values().stream()
                .anyMatch(v -> v.getEmail().email().equalsIgnoreCase(email));
    }

    @Override
    public Vendedor guardar(Vendedor vendedor) {
        vendedores.put(vendedor.getId(), vendedor);
        return vendedor;
    }
}