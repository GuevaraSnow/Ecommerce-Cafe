package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ConsultarPresentacion.
 * (Actor: cualquier visitante) — devuelve el detalle de una Presentación; no modifica nada. Una Presentación
 * dada de baja no existe para quien consulta: responde no encontrado.
 */
@Service
@RequiredArgsConstructor
public class ConsultarPresentacion {

    private final PresentacionRepositorio repositorio;

    @Transactional(readOnly = true)
    public Presentacion ejecutar(String presentacionId) {
        return repositorio.buscarNoEliminadaPorId(presentacionId).orElseThrow();
    }
}
