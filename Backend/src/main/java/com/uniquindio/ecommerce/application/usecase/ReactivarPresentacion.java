package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;

/**
 * Caso de uso: ReactivarPresentacion.
 * (Actor: Vendedor) — reactiva una Presentación que estaba pausada. No tiene lógica: delega en
 * Presentacion.activar().
 */
public class ReactivarPresentacion {

    private final PresentacionRepositorio repositorio;

    public ReactivarPresentacion(PresentacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Presentacion ejecutar(String presentacionId) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        presentacion.activar();
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
