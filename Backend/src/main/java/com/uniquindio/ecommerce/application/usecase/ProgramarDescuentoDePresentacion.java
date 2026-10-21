package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Descuento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ProgramarDescuentoDePresentacion.
 * (Actor: Vendedor) — programa un Descuento por temporada sobre una Presentación. No tiene lógica:
 * delega en Presentacion.programarDescuento().
 */
@Service
@RequiredArgsConstructor
public class ProgramarDescuentoDePresentacion {

    private final PresentacionRepositorio repositorio;

    @Transactional
    public Presentacion ejecutar(String presentacionId, Descuento descuento) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId).orElseThrow();
        presentacion.programarDescuento(descuento);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
