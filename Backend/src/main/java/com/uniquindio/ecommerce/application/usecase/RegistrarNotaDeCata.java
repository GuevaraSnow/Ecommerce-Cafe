package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.NotaDeCata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: RegistrarNotaDeCata.
 * (Actor: Vendedor) — registra los descriptores de sabor de una Presentación Trazable. No tiene
 * lógica: delega en Presentacion.registrarNotaCata(), que rechaza el merchandising.
 */
@Service
@RequiredArgsConstructor
public class RegistrarNotaDeCata {

    private final PresentacionRepositorio repositorio;

    @Transactional
    public Presentacion ejecutar(String presentacionId, NotaDeCata nota) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        presentacion.registrarNotaCata(nota);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
