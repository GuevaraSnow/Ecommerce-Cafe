package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Resena;

import java.util.List;
import java.util.Optional;

public interface ResenaRepositorio {

    Optional<Resena> buscarPorId(String id);

    void guardar(Resena resena);

    /** Reseñas de una Presentación, de la más reciente a la más antigua. */
    List<Resena> buscarPorPresentacion(String presentacionId);

    /** Indica si ese Comprador ya reseñó esa Presentación (solo puede hacerlo una vez). */
    boolean existeResenaDe(String compradorId, String presentacionId);
}
