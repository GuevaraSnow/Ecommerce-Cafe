package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDeNotificacion;

import java.time.LocalDateTime;
import java.util.Objects;

/** Aviso dirigido a un Usuario ante una nueva Reseña, un Reembolso o un cambio de Estado de Compra. */
public class Notificacion {

    private final String id;
    private final String destinatarioId;
    private final TipoDeNotificacion tipo;
    private final String mensaje;
    private final LocalDateTime fecha;
    private boolean leida;

    private Notificacion(String id, String destinatarioId, TipoDeNotificacion tipo, String mensaje,
                          LocalDateTime fecha) {
        this.id = id;
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.leida = false;
    }

    /**
     * Genera una Notificación validando identificador, destinatario, tipo, mensaje y fecha.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Notificacion enviar(String id, String destinatarioId, TipoDeNotificacion tipo, String mensaje,
                                       LocalDateTime fecha) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Notificación debe tener un identificador");
        }
        if (destinatarioId == null || destinatarioId.isBlank()) {
            throw new ReglaDominioException("La Notificación debe tener un destinatario");
        }
        if (tipo == null) {
            throw new ReglaDominioException("La Notificación debe indicar un Tipo de Notificación");
        }
        if (mensaje == null || mensaje.isBlank()) {
            throw new ReglaDominioException("La Notificación debe tener un mensaje");
        }
        if (fecha == null) {
            throw new ReglaDominioException("La Notificación debe tener una fecha");
        }
        return new Notificacion(id, destinatarioId, tipo, mensaje, fecha);
    }

    /** Marca la Notificación como leída. */
    public void marcarLeida() {
        this.leida = true;
    }

    public String getId() {return id;}

    public String getDestinatarioId() {return destinatarioId;}

    public TipoDeNotificacion getTipo() {return tipo;}

    public String getMensaje() {return mensaje;}

    public LocalDateTime getFecha() {return fecha;}

    public boolean isLeida() {return leida;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notificacion)) return false;
        Notificacion otra = (Notificacion) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
