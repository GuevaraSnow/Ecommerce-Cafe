package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;

/**
 * Caso de uso: RetirarStockDePresentacion.
 * (Actor: Vendedor) — retira cantidad del stock de una Presentación (merma, daño, ajuste a la
 * baja); no regresa café al Origen. No tiene lógica: delega en Presentacion.descontarCantidad().
 */
public class RetirarStockDePresentacion {

    private final PresentacionRepositorio repositorio;

    public RetirarStockDePresentacion(PresentacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Presentacion ejecutar(String presentacionId, Cantidad cantidad) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        presentacion.descontarCantidad(cantidad);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
