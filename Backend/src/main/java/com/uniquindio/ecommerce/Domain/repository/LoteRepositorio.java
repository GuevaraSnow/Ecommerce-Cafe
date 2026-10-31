package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;

import java.util.Optional;

public interface LoteRepositorio {

    Optional<Lote> buscarPorCodigo(CodigoDeLote codigo);

    void guardar(Lote lote);

    /**
     * El número más alto ya usado en los códigos de Lote de ese año, para asignar el siguiente al Lote
     * que se va a registrar. Si no hay Lotes ese año, devuelve 0.
     */
    int obtenerNumeroMasAlto(int anio);
}
