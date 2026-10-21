package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: SugerirPresentaciones.
 * (Actor: cualquier visitante) — búsqueda predictiva: mientras el visitante escribe, sugiere
 * Presentaciones disponibles cuyo título contiene el texto; no modifica nada.
 */
@Service
@RequiredArgsConstructor
public class SugerirPresentaciones {

    private final PresentacionRepositorio repositorio;

    @Transactional(readOnly = true)
    public List<Presentacion> ejecutar(String texto, int limite) {
        return repositorio.sugerirDisponiblesPorTitulo(texto, limite);
    }
}
