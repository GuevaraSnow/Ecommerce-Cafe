package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Resena;

import java.util.Optional;

public interface ResenaRepositorio {

    Optional<Resena> buscarPorId(String id);

    void guardar(Resena resena);

    /** Indica si ese Comprador ya reseñó esa Presentación (solo puede hacerlo una vez). */
    boolean existeResenaDe(String compradorId, String presentacionId);
}
