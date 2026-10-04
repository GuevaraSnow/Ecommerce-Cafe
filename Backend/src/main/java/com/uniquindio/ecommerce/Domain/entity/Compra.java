package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Raíz abstracta del agregado Compra: concentra lo común a sus dos
 * subclases, {@link CompraFisica} (Presentaciones con dirección de envío y
 * tramo logístico) y {@link CompraDigital} (Cursos, sin envío).
 */
public abstract class Compra {

    private final String id;
    private final String compradorId;
    private final LocalDate fechaCompra;
    private EstadoDeCompra estado;

    protected Compra(String compradorId) {
        if (compradorId == null || compradorId.isBlank()) {
            throw new ReglaDominioException("La compra debe estar asociada a un comprador");
        }
        this.id = UUID.randomUUID().toString();
        this.compradorId = compradorId;
        this.fechaCompra = LocalDate.now();
        this.estado = EstadoDeCompra.PENDIENTE;
    }

    public void confirmar() {
        if (estado != EstadoDeCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se puede confirmar una compra pendiente (estado actual: " + estado + ")");
        }
        validarConfirmable();
        cambiarEstado(EstadoDeCompra.CONFIRMADA);
    }

    /** Punto de extensión: cada subclase puede exigir condiciones propias antes de confirmar. */
    protected void validarConfirmable() {
    }

    public boolean puedeCancelarse() {
        return estado.puedeTransicionarA(EstadoDeCompra.CANCELADA);
    }

    public void cancelar() {
        if (!puedeCancelarse()) {
            throw new ReglaDominioException("No se puede cancelar una compra en estado " + estado);
        }
        cambiarEstado(EstadoDeCompra.CANCELADA);
    }

    protected void cambiarEstado(EstadoDeCompra nuevo) {
        if (!estado.puedeTransicionarA(nuevo)) {
            throw new ReglaDominioException("Transicion de estado invalida: " + estado + " -> " + nuevo);
        }
        this.estado = nuevo;
    }

    /** Una Compra está activa mientras siga en curso: Pendiente, Confirmada o Enviada. */
    public boolean estaActiva() {
        return estado == EstadoDeCompra.PENDIENTE
                || estado == EstadoDeCompra.CONFIRMADA
                || estado == EstadoDeCompra.ENVIADA;
    }

    /** Indica si la Compra incluye esa Presentación; una Compra digital nunca incluye Presentaciones. */
    public abstract boolean incluye(String presentacionId);

    public String getId() {
        return id;
    }

    public String getCompradorId() {
        return compradorId;
    }

    public EstadoDeCompra getEstado() {
        return estado;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Compra)) return false;
        Compra compra = (Compra) o;
        return id.equals(compra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
