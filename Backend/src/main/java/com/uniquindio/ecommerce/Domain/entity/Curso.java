package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDePublicacion;
import com.uniquindio.ecommerce.Domain.valueobject.NivelDelCurso;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.TematicaDelCurso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Producto digital publicado por un Formador: un conjunto de Lecciones que un
 * Comprador adquiere mediante una Inscripción. A diferencia de una
 * Presentación física, no maneja cantidad disponible ni frescura (por diseño:
 * esas reglas son propias del café físico).
 */
public class Curso {

    private final String id;
    private final String formadorId;
    private final String titulo;
    private final String descripcion;
    private Precio precio;
    private final NivelDelCurso nivel;
    private final TematicaDelCurso tematica;
    private final List<Leccion> lecciones;
    private EstadoDePublicacion estado;
    private boolean eliminadoLogicamente;

    private Curso(String id, String formadorId, String titulo, String descripcion, Precio precio,
                   NivelDelCurso nivel, TematicaDelCurso tematica) {
        this.id = id;
        this.formadorId = formadorId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.nivel = nivel;
        this.tematica = tematica;
        this.lecciones = new ArrayList<>();
        this.estado = EstadoDePublicacion.INACTIVA;
        this.eliminadoLogicamente = false;
    }

    /**
     * Crea un Curso en borrador (sin publicar) validando identificador, Formador,
     * título, precio, nivel y temática. Debe tener al menos una Lección antes
     * de poder publicarse.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Curso crear(String id, String formadorId, String titulo, String descripcion, Precio precio,
                               NivelDelCurso nivel, TematicaDelCurso tematica) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("El Curso debe tener un identificador");
        }
        if (formadorId == null || formadorId.isBlank()) {
            throw new ReglaDominioException("El Curso debe tener un Formador");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new ReglaDominioException("El Curso debe tener un título");
        }
        if (precio == null) {
            throw new ReglaDominioException("El Curso debe tener un Precio");
        }
        if (nivel == null) {
            throw new ReglaDominioException("El Curso debe indicar un Nivel");
        }
        if (tematica == null) {
            throw new ReglaDominioException("El Curso debe indicar una Temática");
        }
        return new Curso(id, formadorId, titulo, descripcion, precio, nivel, tematica);
    }

    /** Agrega una Lección al Curso. */
    public void agregarLeccion(Leccion leccion) {
        validarNoEliminado();
        if (leccion == null) {
            throw new ReglaDominioException("La Lección no puede ser nula");
        }
        if (!leccion.getCursoId().equals(this.id)) {
            throw new ReglaDominioException("La Lección no pertenece a este Curso");
        }
        this.lecciones.add(leccion);
    }

    /** Publica el Curso; exige al menos una Lección con Archivo Digital. */
    public void publicar() {
        validarNoEliminado();
        if (lecciones.isEmpty()) {
            throw new ReglaDominioException("Un Curso debe tener al menos una Lección para publicarse");
        }
        this.estado = EstadoDePublicacion.ACTIVA;
    }

    public void cambiarPrecio(Precio nuevoPrecio) {
        validarNoEliminado();
        if (nuevoPrecio == null) {
            throw new ReglaDominioException("El nuevo Precio no puede ser nulo");
        }
        this.precio = nuevoPrecio;
    }

    /** Da de baja el Curso (borrado lógico); se bloquea si tiene inscritos activos (regla del caso de uso). */
    public void eliminar() {
        this.eliminadoLogicamente = true;
    }

    private void validarNoEliminado() {
        if (eliminadoLogicamente) {
            throw new ReglaDominioException("No se puede operar sobre un Curso eliminado");
        }
    }

    public String getId() {return id;}

    public String getFormadorId() {return formadorId;}

    public String getTitulo() {return titulo;}

    public String getDescripcion() {return descripcion;}

    public Precio getPrecio() {return precio;}

    public NivelDelCurso getNivel() {return nivel;}

    public TematicaDelCurso getTematica() {return tematica;}

    public List<Leccion> getLecciones() {return List.copyOf(lecciones);}

    public EstadoDePublicacion getEstado() {return estado;}

    public boolean isEliminadoLogicamente() {return eliminadoLogicamente;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Curso)) return false;
        Curso otro = (Curso) o;
        return id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
