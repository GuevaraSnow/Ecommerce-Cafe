package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class FincaRepositorioMemoria implements FincaRepositorio {

    private final Map<String, Finca> fagas = new ConcurrentHashMap<>();

    @Override
    public void guardar(Finca finca) {
        if (finca != null) {
            fagas.put(finca.getId(), finca);
        }
    }

    @Override
    public Optional<Finca> buscarPorId(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(fagas.get(id));
    }
}