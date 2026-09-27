package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransformacionRepositorioMemoria implements TransformacionRepositorio {

    private final Map<String, Transformacion> transformaciones = new HashMap<>();

    @Override
    public void guardar(Transformacion transformacion) {
        transformaciones.put(transformacion.getId(), transformacion);
    }

    @Override
    public Optional<Transformacion> buscarPorId(String id) {
        return Optional.ofNullable(transformaciones.get(id));
    }
}
