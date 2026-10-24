package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Reemplazado por CompraRepositorioJpa; con los dos activos Spring lanza NoUniqueBeanDefinitionException
//@Repository
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

    @Override
    public boolean existeCompraActivaConPresentacion(String presentacionId) {
        return compras.values().stream()
                .anyMatch(compra -> compra.estaActiva() && compra.incluye(presentacionId));
    }

    @Override
    public boolean existeCompraEntregada(String compradorId, String presentacionId) {
        return compras.values().stream()
                .anyMatch(compra -> compra.getCompradorId().equals(compradorId)
                        && compra.getEstado() == EstadoDeCompra.ENTREGADA
                        && compra.incluye(presentacionId));
    }
}