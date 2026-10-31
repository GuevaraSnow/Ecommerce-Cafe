package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ConsultarFinca.
 * (Actor: cualquier usuario autenticado) — devuelve una Finca por su id; si no existe, no encontrado.
 * No modifica nada.
 */
@Service
@RequiredArgsConstructor
public class ConsultarFinca {

    private final FincaRepositorio fincaRepositorio;

    @Transactional(readOnly = true)
    public Finca ejecutar(String fincaId) {
        return fincaRepositorio.buscarPorId(fincaId).orElseThrow();
    }
}
