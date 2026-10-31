package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;

import java.util.Optional;

public interface LoteRepositorio {

    Optional<Lote> buscarPorCodigo(CodigoDeLote codigo);

    void guardar(Lote lote);

    /** Obtiene el numero mas alto de ese año para asignarselo al nuevo lote a registrar,
     si no hay lotes ese año devuelve 0 */
    int obtenerNumeroMasAlto(int anio);
}
