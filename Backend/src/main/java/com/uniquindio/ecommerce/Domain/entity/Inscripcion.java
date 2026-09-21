package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Acceso de un Comprador a un Curso, originado por una Compra. Controla si
 * el contenido ya fue accedido (lo que bloquea el reembolso) y si el acceso
 * sigue vigente.
 */
public class Inscripcion {
    private static final int HORAS_LIMITE_REEMBOLSO = 48;

    private final String id;
    private final String compradorId;
    private final String cursoId;
    private final String compraId;
    private final LocalDate fechaInscripcion;
    private boolean accedido;
    private LocalDate fechaPrimerAcceso;
    private boolean revocada;

    private Inscripcion(String id, String compradorId, String cursoId, String compraId,
                         LocalDate fechaInscripcion) {
        this.id = id;
        this.compradorId = compradorId;
        this.cursoId = cursoId;
        this.compraId = compraId;
        this.fechaInscripcion = fechaInscripcion;
        this.accedido = false;
        this.fechaPrimerAcceso = null;
        this.revocada = false;
    }

    /**
     * Crea una Inscripción validando Comprador, Curso, Compra de origen y fecha.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Inscripcion crear(String id, String compradorId, String cursoId, String compraId,
                                     LocalDate fechaInscripcion) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Inscripción debe tener un identificador");
        }
        if (compradorId == null || compradorId.isBlank()) {
            throw new ReglaDominioException("La Inscripción debe tener un Comprador");
        }
        if (cursoId == null || cursoId.isBlank()) {
            throw new ReglaDominioException("La Inscripción debe tener un Curso");
        }
        if (compraId == null || compraId.isBlank()) {
            throw new ReglaDominioException("La Inscripción debe originarse de una Compra");
        }
        if (fechaInscripcion == null) {
            throw new ReglaDominioException("La Inscripción debe tener una fecha");
        }
        return new Inscripcion(id, compradorId, cursoId, compraId, fechaInscripcion);
    }

    /** Registra que el comprador accedió al contenido; solo guarda la fecha la primera vez. */
    public void registrarAcceso(LocalDate fecha) {
        validarNoRevocada();
        if (!this.accedido) {
            this.accedido = true;
            this.fechaPrimerAcceso = fecha;
        }
    }

    /**
     * Indica si la Inscripción todavía puede reembolsarse: dentro de las 48
     * horas siguientes a la compra y sin haber accedido al contenido.
     */
    public boolean puedeReembolsarse() {
        if (accedido || revocada) {
            return false;
        }
        long horasTranscurridas = ChronoUnit.DAYS.between(fechaInscripcion, LocalDate.now()) * 24;
        return horasTranscurridas <= HORAS_LIMITE_REEMBOLSO;
    }

    /** Revoca el acceso al Curso (ej. al aprobarse un reembolso). */
    public void revocar() {
        this.revocada = true;
    }

    private void validarNoRevocada() {
        if (revocada) {
            throw new ReglaDominioException("No se puede acceder a una Inscripción revocada");
        }
    }

    public String getId() {return id;}

    public String getCompradorId() {return compradorId;}

    public String getCursoId() {return cursoId;}

    public String getCompraId() {return compraId;}

    public LocalDate getFechaInscripcion() {return fechaInscripcion;}

    public boolean isAccedido() {return accedido;}

    public LocalDate getFechaPrimerAcceso() {return fechaPrimerAcceso;}

    public boolean isRevocada() {return revocada;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inscripcion)) return false;
        Inscripcion otra = (Inscripcion) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
