package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class PresentacionRepositorioMemoria implements PresentacionRepositorio {
    private final Map<String, Presentacion> presentaciones = new HashMap<>();

    @Override
    public Optional<Presentacion> buscarPorId(String id) {
        return Optional.ofNullable(presentaciones.get(id));
    }

    @Override
    public void guardar(Presentacion presentacion) {
        presentaciones.put(presentacion.getId(), presentacion);
    }
}
