package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReposicion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;

/**
 * Caso de uso: ReponerStockDePresentacion.
 * (Actor: Vendedor) — repone stock de una Presentación; si es Trazable, el café sale de su Origen.
 * No tiene lógica: delega en el Domain Service {@link ServicioDeReposicion}.
 */
public class ReponerStockDePresentacion {

    private final PresentacionRepositorio repositorio;
    private final ServicioDeReposicion servicioDeReposicion;

    public ReponerStockDePresentacion(PresentacionRepositorio repositorio,
                                      ServicioDeReposicion servicioDeReposicion) {
        this.repositorio = repositorio;
        this.servicioDeReposicion = servicioDeReposicion;
    }

    public Presentacion ejecutar(String presentacionId, Cantidad cantidad) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        servicioDeReposicion.reponer(presentacion, cantidad);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
