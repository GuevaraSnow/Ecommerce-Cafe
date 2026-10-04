package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.entity.Leccion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CursoRepositorio;
import com.uniquindio.ecommerce.Domain.repository.InscripcionRepositorio;

import java.time.LocalDate;

/**
 * Caso de uso: AccederLeccion.
 * (Actor: Comprador) — accede al contenido de una Lección de un Curso; solo
 * es posible con una Inscripción vigente (no revocada). Registra el primer
 * acceso, lo que más adelante bloquea el reembolso del Curso.
 */
public class AccederLeccion {

    private final InscripcionRepositorio inscripcionRepositorio;
    private final CursoRepositorio cursoRepositorio;

    public AccederLeccion(InscripcionRepositorio inscripcionRepositorio, CursoRepositorio cursoRepositorio) {
        this.inscripcionRepositorio = inscripcionRepositorio;
        this.cursoRepositorio = cursoRepositorio;
    }

    public Leccion ejecutar(String compradorId, String cursoId, int numeroOrdenLeccion) {
        Inscripcion inscripcion = inscripcionRepositorio.buscarActivaPorCompradorYCurso(compradorId, cursoId)
                .orElseThrow(() -> new ReglaDominioException(
                        "El Comprador no tiene una Inscripción vigente a este Curso"));

        Curso curso = cursoRepositorio.buscarPorId(cursoId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Curso con id " + cursoId));

        Leccion leccion = curso.getLecciones().stream()
                .filter(l -> l.getNumeroOrden() == numeroOrdenLeccion)
                .findFirst()
                .orElseThrow(() -> new ReglaDominioException(
                        "El Curso no tiene una Lección con el número de orden " + numeroOrdenLeccion));

        inscripcion.registrarAcceso(LocalDate.now());
        inscripcionRepositorio.guardar(inscripcion);

        return leccion;
    }
}
