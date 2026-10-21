package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;

public class ConfirmarCompra {

    private final CompraRepositorio repositorioCompra;

    public ConfirmarCompra(CompraRepositorio repositorioCompra) {
        this.repositorioCompra = repositorioCompra;
    }

    /**
     * Confirma una Compra pendiente con la Dirección de Envío del Comprador; una
     * misma Compra no puede confirmarse dos veces.
     */
    public Compra ejecutar(String compraId, String ciudad, String direccion, String destinatario) {
        Compra compra = repositorioCompra.buscarPorId(compraId)
                .orElseThrow(() -> new ReglaDominioException("Compra no encontrada: " + compraId));
        compra.confirmar(new DireccionDeEnvio(ciudad, direccion, destinatario));
        repositorioCompra.guardar(compra);
        return compra;
    }
}