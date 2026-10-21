package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ReactivarPresentacion.
 * (Actor: Vendedor) — reactiva una Presentación que estaba pausada. No tiene lógica: delega en
 * Presentacion.activar().
 */
@Service
@RequiredArgsConstructor
public class ReactivarPresentacion {

    private final PresentacionRepositorio repositorio;

    @Transactional
    public Presentacion ejecutar(String presentacionId) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow();
        presentacion.activar();
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
