package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Finca;

import java.util.Optional;

public interface FincaRepositorio {
    void guardar(Finca finca);
    Optional<Finca> buscarPorId(String id);
}