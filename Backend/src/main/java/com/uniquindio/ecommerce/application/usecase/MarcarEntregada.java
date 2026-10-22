package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso: MarcarEntregada.
 * (Actor: Vendedor) — marca como entregada una Compra enviada y registra la fecha y hora
 * de entrega (desde ahí corre el plazo de reembolso). No tiene lógica: delega en Compra.entregar().
 */
@Service
@RequiredArgsConstructor
public class MarcarEntregada {

    private final CompraRepositorio compraRepositorio;

    @Transactional
    public Compra ejecutar(String compraId) {
        Compra compra = compraRepositorio.buscarPorId(compraId).orElseThrow();
        compra.entregar(LocalDateTime.now());
        compraRepositorio.guardar(compra);
        return compra;
    }
}
