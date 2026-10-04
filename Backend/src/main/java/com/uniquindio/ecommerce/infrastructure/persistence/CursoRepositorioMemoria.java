package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.repository.CursoRepositorio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CursoRepositorioMemoria implements CursoRepositorio {

    private final Map<String, Curso> cursos = new HashMap<>();

    @Override
    public Optional<Curso> buscarPorId(String id) {
        return Optional.ofNullable(cursos.get(id));
    }

    @Override
    public void guardar(Curso curso) {
        cursos.put(curso.getId(), curso);
    }
}
