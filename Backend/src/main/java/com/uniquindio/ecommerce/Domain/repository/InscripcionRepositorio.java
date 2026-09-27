package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Inscripcion;

import java.util.Optional;

public interface InscripcionRepositorio {

    Optional<Inscripcion> buscarPorId(String id);

    /** Busca una Inscripción no revocada de ese Comprador a ese Curso (para bloquear doble compra). */
    Optional<Inscripcion> buscarActivaPorCompradorYCurso(String compradorId, String cursoId);

    void guardar(Inscripcion inscripcion);
}
