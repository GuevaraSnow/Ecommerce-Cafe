package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.entity.Leccion;
import com.uniquindio.ecommerce.Domain.repository.CursoRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.NivelDelCurso;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.TematicaDelCurso;

import java.util.List;

/**
 * Caso de uso: PublicarCurso.
 * (Actor: Formador) — publica un Curso; exige al menos una Lección con
 * Archivo Digital.
 */
public class PublicarCurso {

    private final CursoRepositorio repositorio;

    public PublicarCurso(CursoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Curso ejecutar(String id, String formadorId, String titulo, String descripcion, Precio precio,
                          NivelDelCurso nivel, TematicaDelCurso tematica, List<Leccion> lecciones) {
        Curso curso = Curso.crear(id, formadorId, titulo, descripcion, precio, nivel, tematica);
        for (Leccion leccion : lecciones == null ? List.<Leccion>of() : lecciones) {
            curso.agregarLeccion(leccion);
        }
        curso.publicar();
        repositorio.guardar(curso);
        return curso;
    }
}
