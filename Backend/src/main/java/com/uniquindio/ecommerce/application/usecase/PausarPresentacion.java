package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;

/**
 * Caso de uso: PausarPresentacion.
 * (Actor: Vendedor) — pausa la venta de una Presentación activa. No tiene lógica: delega en
 * Presentacion.desactivar().
 */
public class PausarPresentacion {

    private final PresentacionRepositorio repositorio;

    public PausarPresentacion(PresentacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Presentacion ejecutar(String presentacionId) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        presentacion.desactivar();
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
