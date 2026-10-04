package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CursoRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.ArchivoDigital;
import com.uniquindio.ecommerce.Domain.valueobject.Duracion;

/**
 * Caso de uso: AgregarLeccion.
 * (Actor: Formador) — agrega una Lección en una posición del Curso,
 * renumerando las que quedan después (regla de reordenamiento por inserción).
 */
public class AgregarLeccion {

    private final CursoRepositorio repositorio;

    public AgregarLeccion(CursoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Curso ejecutar(String cursoId, int posicion, String titulo, ArchivoDigital archivo, Duracion duracion) {
        Curso curso = repositorio.buscarPorId(cursoId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Curso con id " + cursoId));

        curso.insertarLeccion(posicion, titulo, archivo, duracion);
        repositorio.guardar(curso);
        return curso;
    }
}
