package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Pedido de un Comprador para que se le reembolse una Compra. Nace en estado
 * SOLICITADO y solo puede resolverse una vez (aprobada o rechazada).
 */
public class SolicitudReembolso {

    private final String id;
    private final String compraId;
    private final String motivo;
    private final LocalDate fechaSolicitud;
    private EstadoDeReembolso estado;
    private LocalDate fechaResolucion;

    private SolicitudReembolso(String id, String compraId, String motivo, LocalDate fechaSolicitud) {
        this.id = id;
        this.compraId = compraId;
        this.motivo = motivo;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = EstadoDeReembolso.SOLICITADO;
        this.fechaResolucion = null;
    }

    /**
     * Crea una Solicitud de Reembolso validando identificador, Compra asociada,
     * motivo y fecha de solicitud.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static SolicitudReembolso solicitar(String id, String compraId, String motivo, LocalDate fechaSolicitud) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Solicitud de Reembolso debe tener un identificador");
        }
        if (compraId == null || compraId.isBlank()) {
            throw new ReglaDominioException("La Solicitud de Reembolso debe asociarse a una Compra");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaDominioException("La Solicitud de Reembolso debe indicar un motivo");
        }
        if (fechaSolicitud == null) {
            throw new ReglaDominioException("La Solicitud de Reembolso debe tener una fecha de solicitud");
        }
        return new SolicitudReembolso(id, compraId, motivo, fechaSolicitud);
    }

    /** Aprueba la solicitud; solo es posible si sigue SOLICITADA. */
    public void aprobar(LocalDate fechaResolucion) {
        validarPendiente();
        this.estado = EstadoDeReembolso.APROBADO;
        this.fechaResolucion = fechaResolucion;
    }

    /** Rechaza la solicitud; solo es posible si sigue SOLICITADA. */
    public void rechazar(LocalDate fechaResolucion) {
        validarPendiente();
        this.estado = EstadoDeReembolso.RECHAZADO;
        this.fechaResolucion = fechaResolucion;
    }

    private void validarPendiente() {
        if (estado != EstadoDeReembolso.SOLICITADO) {
            throw new ReglaDominioException("Solo se puede resolver una Solicitud de Reembolso que esté SOLICITADA");
        }
    }

    public String getId() {return id;}

    public String getCompraId() {return compraId;}

    public String getMotivo() {return motivo;}

    public LocalDate getFechaSolicitud() {return fechaSolicitud;}

    public EstadoDeReembolso getEstado() {return estado;}

    public LocalDate getFechaResolucion() {return fechaResolucion;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolicitudReembolso)) return false;
        SolicitudReembolso otra = (SolicitudReembolso) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
