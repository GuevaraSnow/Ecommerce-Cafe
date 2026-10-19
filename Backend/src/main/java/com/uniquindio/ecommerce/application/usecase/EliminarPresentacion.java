package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeBajaDePresentacion;

/**
 * Caso de uso: EliminarPresentacion.
 * (Actor: Vendedor) — da de baja lógica una Presentación. No tiene lógica:
 * la regla de las compras activas la verifica el Domain Service
 * {@link ServicioDeBajaDePresentacion}.
 */
public class EliminarPresentacion {

    private final PresentacionRepositorio presentacionRepositorio;
    private final ServicioDeBajaDePresentacion servicioDeBaja;

    public EliminarPresentacion(PresentacionRepositorio presentacionRepositorio,
                                 ServicioDeBajaDePresentacion servicioDeBaja) {
        this.presentacionRepositorio = presentacionRepositorio;
        this.servicioDeBaja = servicioDeBaja;
    }

    public Presentacion ejecutar(String presentacionId) {
        Presentacion presentacion = presentacionRepositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        servicioDeBaja.darDeBaja(presentacion);
        presentacionRepositorio.guardar(presentacion);
        return presentacion;
    }
}
