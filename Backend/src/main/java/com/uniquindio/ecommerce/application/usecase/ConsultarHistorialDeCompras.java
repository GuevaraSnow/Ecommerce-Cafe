package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: ConsultarHistorialDeCompras.
 * (Actor: Comprador) — lista sus Compras, de la más reciente a la más antigua; no modifica nada.
 */
@Service
@RequiredArgsConstructor
public class ConsultarHistorialDeCompras {

    private final CompraRepositorio repositorio;

    @Transactional(readOnly = true)
    public List<Compra> ejecutar(String compradorId) {
        return repositorio.buscarPorComprador(compradorId);
    }
}
