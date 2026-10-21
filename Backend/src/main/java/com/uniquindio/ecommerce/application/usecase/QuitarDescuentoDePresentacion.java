package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: QuitarDescuentoDePresentacion.
 * (Actor: Vendedor) — quita el Descuento programado de una Presentación. No tiene lógica:
 * delega en Presentacion.quitarDescuento().
 */
@Service
@RequiredArgsConstructor
public class QuitarDescuentoDePresentacion {

    private final PresentacionRepositorio repositorio;

    @Transactional
    public Presentacion ejecutar(String presentacionId) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId).orElseThrow();
        presentacion.quitarDescuento();
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
