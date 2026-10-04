package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.repository.InscripcionRepositorio;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InscripcionRepositorioMemoria implements InscripcionRepositorio {

    private final Map<String, Inscripcion> inscripciones = new HashMap<>();

    @Override
    public Optional<Inscripcion> buscarPorId(String id) {
        return Optional.ofNullable(inscripciones.get(id));
    }

    @Override
    public Optional<Inscripcion> buscarActivaPorCompradorYCurso(String compradorId, String cursoId) {
        return inscripciones.values().stream()
                .filter(inscripcion -> inscripcion.getCompradorId().equals(compradorId))
                .filter(inscripcion -> inscripcion.getCursoId().equals(cursoId))
                .filter(inscripcion -> !inscripcion.isRevocada())
                .findFirst();
    }

    @Override
    public void guardar(Inscripcion inscripcion) {
        inscripciones.put(inscripcion.getId(), inscripcion);
    }
}
