package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Compra;

import java.util.Optional;

public interface CompraRepositorio {
    Optional<Compra> buscarPorId(String id);

    void guardar(Compra compra);

    /** Indica si alguna Compra activa incluye esa Presentación (para bloquear su eliminación). */
    boolean existeCompraActivaConPresentacion(String presentacionId);

    /** Indica si ese Comprador tiene una Compra ENTREGADA que incluye esa Presentación (para poder reseñarla). */
    boolean existeCompraEntregada(String compradorId, String presentacionId);
}