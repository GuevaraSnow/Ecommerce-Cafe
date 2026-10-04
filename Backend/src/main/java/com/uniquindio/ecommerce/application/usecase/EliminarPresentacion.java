package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;

/**
 * Caso de uso: EliminarPresentacion.
 * (Actor: Vendedor) — da de baja lógica una Presentación; se bloquea si
 * tiene compras activas.
 */
public class EliminarPresentacion {

    private final PresentacionRepositorio presentacionRepositorio;
    private final CompraRepositorio compraRepositorio;

    public EliminarPresentacion(PresentacionRepositorio presentacionRepositorio,
                                 CompraRepositorio compraRepositorio) {
        this.presentacionRepositorio = presentacionRepositorio;
        this.compraRepositorio = compraRepositorio;
    }

    public Presentacion ejecutar(String presentacionId) {
        Presentacion presentacion = presentacionRepositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));

        if (compraRepositorio.existeCompraActivaConPresentacion(presentacionId)) {
            throw new ReglaDominioException("No se puede eliminar una Presentación con compras activas");
        }

        presentacion.darDeBaja();
        presentacionRepositorio.guardar(presentacion);
        return presentacion;
    }
}
