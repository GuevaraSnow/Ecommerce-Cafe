package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Pedido de un Comprador para que se le reembolse UNA Presentación de una Compra entregada.
 * Pedir el reembolso de todo el carrito crea una solicitud por cada Presentación. Guarda el
 * vendedor dueño de la Presentación, porque solo él la resuelve. Nace SOLICITADA y se resuelve
 * una sola vez (aprobada o rechazada). La Compra, la Presentación y el vendedor se referencian por id.
 */
public class SolicitudReembolso {

    private final String id;
    private final String compraId;
    private final String presentacionId;
    private final String vendedorId;
    private final String motivo;
    private final LocalDateTime fechaSolicitud;
    private EstadoDeReembolso estado;
    private LocalDateTime fechaResolucion;

    private SolicitudReembolso(String id, String compraId, String presentacionId, String vendedorId,
                               String motivo, LocalDateTime fechaSolicitud,
                               EstadoDeReembolso estado, LocalDateTime fechaResolucion) {
        this.id = id;
        this.compraId = compraId;
        this.presentacionId = presentacionId;
        this.vendedorId = vendedorId;
        this.motivo = motivo;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.fechaResolucion = fechaResolucion;
    }

    /**
     * Crea la Solicitud de Reembolso de una Presentación de una Compra. Que la Compra esté
     * entregada, dentro de las 48 h y sea de quien la pide lo verifica ServicioDeReembolso.
     *
     * @throws ReglaDominioException si falta algún dato
     */
    public static SolicitudReembolso solicitar(String id, String compraId, String presentacionId, String vendedorId,
                                               String motivo, LocalDateTime fechaSolicitud) {
        validarTexto(id, "La Solicitud de Reembolso debe tener un identificador");
        validarTexto(compraId, "La Solicitud de Reembolso debe asociarse a una Compra");
        validarTexto(presentacionId, "La Solicitud de Reembolso debe indicar la Presentacion a reembolsar");
        validarTexto(vendedorId, "La Solicitud de Reembolso debe conocer al vendedor de la Presentacion");
        validarTexto(motivo, "La Solicitud de Reembolso debe indicar un motivo");
        if (fechaSolicitud == null) {
            throw new ReglaDominioException("La Solicitud de Reembolso debe tener una fecha de solicitud");
        }
        return new SolicitudReembolso(id, compraId, presentacionId, vendedorId, motivo, fechaSolicitud,
                EstadoDeReembolso.SOLICITADO, null);
    }

    /**
     * Revive una Solicitud ya guardada, tal como quedó. No valida: solo la usa la persistencia.
     */
    public static SolicitudReembolso reconstruir(String id, String compraId, String presentacionId, String vendedorId,
                                                 String motivo, LocalDateTime fechaSolicitud,
                                                 EstadoDeReembolso estado, LocalDateTime fechaResolucion) {
        return new SolicitudReembolso(id, compraId, presentacionId, vendedorId, motivo, fechaSolicitud,
                estado, fechaResolucion);
    }

    /** Aprueba la solicitud; solo es posible si sigue SOLICITADA. */
    public void aprobar(LocalDateTime fechaResolucion) {
        resolver(EstadoDeReembolso.APROBADO, fechaResolucion);
    }

    /** Rechaza la solicitud; solo es posible si sigue SOLICITADA. */
    public void rechazar(LocalDateTime fechaResolucion) {
        resolver(EstadoDeReembolso.RECHAZADO, fechaResolucion);
    }

    private void resolver(EstadoDeReembolso nuevoEstado, LocalDateTime fechaResolucion) {
        if (estado != EstadoDeReembolso.SOLICITADO) {
            throw new ReglaDominioException("Solo se puede resolver una Solicitud de Reembolso que este SOLICITADA");
        }
        if (fechaResolucion == null) {
            throw new ReglaDominioException("La resolucion debe registrar la fecha en que se tomo");
        }
        this.estado = nuevoEstado;
        this.fechaResolucion = fechaResolucion;
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException(mensaje);
        }
    }

    public String getId() {
        return id;
    }

    public String getCompraId() {
        return compraId;
    }

    public String getPresentacionId() {
        return presentacionId;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public EstadoDeReembolso getEstado() {
        return estado;
    }

    /** Vacía mientras la solicitud no se haya resuelto. */
    public Optional<LocalDateTime> getFechaResolucion() {
        return Optional.ofNullable(fechaResolucion);
    }

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
