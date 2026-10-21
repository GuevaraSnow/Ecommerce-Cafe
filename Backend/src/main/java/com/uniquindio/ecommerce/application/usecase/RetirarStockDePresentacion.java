package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: RetirarStockDePresentacion.
 * (Actor: Vendedor) — retira cantidad del stock de una Presentación (merma, daño, ajuste a la
 * baja); no regresa café al Origen. No tiene lógica: delega en Presentacion.descontarCantidad().
 */
@Service
@RequiredArgsConstructor
public class RetirarStockDePresentacion {

    private final PresentacionRepositorio repositorio;

    @Transactional
    public Presentacion ejecutar(String presentacionId, Cantidad cantidad) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow();
        presentacion.descontarCantidad(cantidad);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
