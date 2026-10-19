package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReposicion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ReponerStockDePresentacion.
 * (Actor: Vendedor) — repone stock de una Presentación; si es Trazable, el café sale de su Origen.
 * No tiene lógica: delega en el Domain Service {@link ServicioDeReposicion}.
 */
@Service
@RequiredArgsConstructor
public class ReponerStockDePresentacion {

    private final PresentacionRepositorio repositorio;
    private final ServicioDeReposicion servicioDeReposicion;

    @Transactional
    public Presentacion ejecutar(String presentacionId, Cantidad cantidad) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        servicioDeReposicion.reponer(presentacion, cantidad);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
