package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;

public class ConfirmarCompra {

    private final CompraRepositorio repositorioCompra;

    public ConfirmarCompra(CompraRepositorio repositorioCompra) {
        this.repositorioCompra = repositorioCompra;
    }

    /** Confirma una Compra pendiente; una misma Compra no puede confirmarse dos veces. */
    public Compra ejecutar(String compraId) {
        Compra compra = repositorioCompra.buscarPorId(compraId)
                .orElseThrow(() -> new ReglaDominioException("Compra no encontrada: " + compraId));
        compra.confirmar();
        repositorioCompra.guardar(compra);
        return compra;
    }
}