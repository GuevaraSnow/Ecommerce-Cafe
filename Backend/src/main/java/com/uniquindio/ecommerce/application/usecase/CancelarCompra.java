package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: CancelarCompra.
 * (Actor: Comprador) — cancela una Compra que aún no se envió. No tiene lógica:
 * {@link ServicioDeCompra} repone el stock si la Compra ya estaba confirmada.
 */
@Service
@RequiredArgsConstructor
public class CancelarCompra {

    private final CompraRepositorio compraRepositorio;
    private final ServicioDeCompra servicioDeCompra;

    @Transactional
    public Compra ejecutar(String compraId) {
        Compra compra = compraRepositorio.buscarPorId(compraId).orElseThrow();
        servicioDeCompra.cancelar(compra);
        compraRepositorio.guardar(compra);
        return compra;
    }
}
