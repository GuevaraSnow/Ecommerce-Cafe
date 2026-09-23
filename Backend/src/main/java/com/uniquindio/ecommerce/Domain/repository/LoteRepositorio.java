package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;

import java.util.Optional;

public interface LoteRepositorio {

    Optional<Lote> buscarPorCodigo(CodigoDeLote codigo);

    void guardar(Lote lote);
}
