package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;

/**
 * Caso de uso: CambiarPrecioDePresentacion.
 * (Actor: Vendedor) — cambia el precio de una Presentación publicada. No tiene lógica: delega en
 * Presentacion.cambiarPrecio().
 */
public class CambiarPrecioDePresentacion {

    private final PresentacionRepositorio repositorio;

    public CambiarPrecioDePresentacion(PresentacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Presentacion ejecutar(String presentacionId, Precio nuevoPrecio) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        presentacion.cambiarPrecio(nuevoPrecio);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
