package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;

import java.util.Optional;

public interface TransformacionRepositorio {
    void guardar(Transformacion transformacion);
    Optional<Transformacion> buscarPorId(String id);
}