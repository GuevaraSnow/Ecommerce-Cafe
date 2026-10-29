package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Reemplazado por ResenaRepositorioJpa; con los dos activos Spring lanza NoUniqueBeanDefinitionException
//@Repository
public class ResenaRepositorioMemoria implements ResenaRepositorio {

    private final Map<String, Resena> resenas = new HashMap<>();

    @Override
    public Optional<Resena> buscarPorId(String id) {
        return Optional.ofNullable(resenas.get(id));
    }

    @Override
    public void guardar(Resena resena) {
        resenas.put(resena.getId(), resena);
    }

    @Override
    public List<Resena> buscarPorPresentacion(String presentacionId) {
        return resenas.values().stream()
                .filter(resena -> resena.getPresentacionId().equals(presentacionId))
                .sorted(Comparator.comparing(Resena::getFecha).reversed())
                .toList();
    }

    @Override
    public boolean existeResenaDe(String compradorId, String presentacionId) {
        return resenas.values().stream()
                .anyMatch(resena -> resena.getCompradorId().equals(compradorId)
                        && resena.getPresentacionId().equals(presentacionId));
    }
}
