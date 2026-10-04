package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Curso;

import java.util.Optional;

public interface CursoRepositorio {

    Optional<Curso> buscarPorId(String id);

    void guardar(Curso curso);
}
