package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.CompraFisica;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;

public class CambiarEstadoCompra {

    private final CompraRepositorio repositorioCompra;

    public CambiarEstadoCompra(CompraRepositorio repositorioCompra) {
        this.repositorioCompra = repositorioCompra;
    }

    /**
     * Avanza una Compra por el tramo logistico (ENVIADA, ENTREGADA). Confirmar
     * y cancelar tienen su propio caso de uso: cancelar debe devolver el stock
     * y no puede saltarse desde aqui. Solo aplica a compras fisicas: una
     * compra digital no tiene tramo logistico.
     */
    public CompraFisica ejecutar(String compraId, EstadoDeCompra nuevoEstado) {
        if (nuevoEstado == null) {
            throw new ReglaDominioException("Debe indicar el nuevo estado de la compra");
        }
        Compra encontrada = repositorioCompra.buscarPorId(compraId)
                .orElseThrow(() -> new ReglaDominioException("Compra no encontrada: " + compraId));
        if (!(encontrada instanceof CompraFisica compra)) {
            throw new ReglaDominioException("Solo una compra fisica tiene tramo logistico: " + compraId);
        }

        switch (nuevoEstado) {
            case ENVIADA -> compra.marcarEnviada();
            case ENTREGADA -> compra.marcarEntregada();
            default -> throw new ReglaDominioException(
                    "Este caso de uso solo mueve una compra a ENVIADA o ENTREGADA; para "
                            + nuevoEstado + " use su caso de uso especifico");
        }
        repositorioCompra.guardar(compra);
        return compra;
    }
}