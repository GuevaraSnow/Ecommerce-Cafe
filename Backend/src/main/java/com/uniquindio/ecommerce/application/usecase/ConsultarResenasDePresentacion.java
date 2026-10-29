package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: ConsultarResenasDePresentacion.
 * (Actor: cualquier visitante) — lista las reseñas de una Presentación, de la más reciente a la más antigua.
 */
@Service
@RequiredArgsConstructor
public class ConsultarResenasDePresentacion {

    private final ResenaRepositorio repositorio;

    @Transactional(readOnly = true)
    public List<Resena> ejecutar(String presentacionId) {
        return repositorio.buscarPorPresentacion(presentacionId);
    }
}
