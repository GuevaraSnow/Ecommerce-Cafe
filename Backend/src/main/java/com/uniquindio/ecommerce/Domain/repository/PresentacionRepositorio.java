package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;

import java.util.Optional;

public interface PresentacionRepositorio {
    Optional<Presentacion> buscarPorId(String id);

    void guardar(Presentacion presentacion);
}
