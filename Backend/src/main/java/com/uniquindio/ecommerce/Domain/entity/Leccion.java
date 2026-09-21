package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.ArchivoDigital;
import com.uniquindio.ecommerce.Domain.valueobject.Duracion;

import java.util.Objects;

/** Unidad de contenido dentro de un Curso; se identifica por su número de orden dentro del Curso. */
public class Leccion {

    private final String cursoId;
    private final int numeroOrden;
    private final String titulo;
    private ArchivoDigital archivo;
    private final Duracion duracion;

    private Leccion(String cursoId, int numeroOrden, String titulo, ArchivoDigital archivo, Duracion duracion) {
        this.cursoId = cursoId;
        this.numeroOrden = numeroOrden;
        this.titulo = titulo;
        this.archivo = archivo;
        this.duracion = duracion;
    }

    /**
     * Crea una Lección validando su Curso, orden, título, Archivo Digital y Duración.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Leccion crear(String cursoId, int numeroOrden, String titulo, ArchivoDigital archivo,
                                 Duracion duracion) {
        if (cursoId == null || cursoId.isBlank()) {
            throw new ReglaDominioException("La Lección debe pertenecer a un Curso");
        }
        if (numeroOrden <= 0) {
            throw new ReglaDominioException("El número de orden de la Lección debe ser mayor a cero");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new ReglaDominioException("La Lección debe tener un título");
        }
        if (archivo == null) {
            throw new ReglaDominioException("La Lección debe tener un Archivo Digital");
        }
        if (duracion == null) {
            throw new ReglaDominioException("La Lección debe tener una Duración");
        }
        return new Leccion(cursoId, numeroOrden, titulo, archivo, duracion);
    }

    /** Reemplaza el archivo de la Lección (ej. al resubir el video). */
    public void reemplazarArchivo(ArchivoDigital nuevoArchivo) {
        if (nuevoArchivo == null) {
            throw new ReglaDominioException("El nuevo Archivo Digital no puede ser nulo");
        }
        this.archivo = nuevoArchivo;
    }

    public String getCursoId() {return cursoId;}

    public int getNumeroOrden() {return numeroOrden;}

    public String getTitulo() {return titulo;}

    public ArchivoDigital getArchivo() {return archivo;}

    public Duracion getDuracion() {return duracion;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leccion)) return false;
        Leccion otra = (Leccion) o;
        return cursoId.equals(otra.cursoId) && numeroOrden == otra.numeroOrden;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cursoId, numeroOrden);
    }
}
