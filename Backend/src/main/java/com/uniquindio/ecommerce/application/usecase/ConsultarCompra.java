package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ConsultarCompra.
 * (Actor: Comprador dueño o vendedor de la Compra) — devuelve el detalle de una Compra; no modifica nada.
 */
@Service
@RequiredArgsConstructor
public class ConsultarCompra {

    private final CompraRepositorio repositorio;

    @Transactional(readOnly = true)
    public Compra ejecutar(String compraId) {
        return repositorio.buscarPorId(compraId).orElseThrow();
    }
}
