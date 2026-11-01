package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;

/**
 * Domain Service que protege la regla 2: un Comprador solo puede reseñar una Presentación
 * que adquirió (tiene una Compra ENTREGADA que la incluye), y una sola vez. La regla cruza
 * agregados (Reseña y Compra), por eso no vive en la entidad. Sin estado y sin anotaciones de Spring.
 */
public class VerificadorDeCompra {

    private final CompraRepositorio compraRepositorio;
    private final ResenaRepositorio resenaRepositorio;

    public VerificadorDeCompra(CompraRepositorio compraRepositorio, ResenaRepositorio resenaRepositorio) {
        this.compraRepositorio = compraRepositorio;
        this.resenaRepositorio = resenaRepositorio;
    }

    /**
     * Verifica, sin modificar nada, que el Comprador puede reseñar la Presentación.
     *
     * @throws ReglaDominioException si no la adquirió o si ya la reseñó
     */
    public void verificarQuePuedeResenar(String compradorId, String presentacionId) {
        if (!compraRepositorio.existeCompraEntregada(compradorId, presentacionId)) {
            throw new ReglaDominioException("Solo puede reseñar una presentacion quien la adquirio (compra entregada)");
        }
        if (resenaRepositorio.existeResenaDe(compradorId, presentacionId)) {
            throw new ReglaDominioException("El comprador ya reseño esta presentacion");
        }
    }
}
