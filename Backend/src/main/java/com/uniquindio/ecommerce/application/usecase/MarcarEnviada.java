package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: MarcarEnviada.
 * (Actor: Vendedor) — marca como enviada una Compra confirmada. No tiene lógica:
 * delega en Compra.enviar().
 */
@Service
@RequiredArgsConstructor
public class MarcarEnviada {

    private final CompraRepositorio compraRepositorio;

    @Transactional
    public Compra ejecutar(String compraId) {
        Compra compra = compraRepositorio.buscarPorId(compraId).orElseThrow();
        compra.enviar();
        compraRepositorio.guardar(compra);
        return compra;
    }
}
