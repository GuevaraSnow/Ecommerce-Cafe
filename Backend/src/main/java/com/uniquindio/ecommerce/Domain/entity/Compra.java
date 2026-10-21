package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz del agregado Compra: un pedido de Presentaciones con sus Detalles (cada
 * uno con el precio congelado), su Dirección de Envío y su tramo logístico.
 * Mientras está PENDIENTE la Compra es el carrito del Comprador; la Dirección
 * de Envío se fija al confirmarla. Las Presentaciones se referencian por id.
 */
public class Compra {

    private final String id;
    private final String compradorId;
    private final LocalDate fechaCompra;
    private final List<DetalleDeCompra> detalles;
    private EstadoDeCompra estado;
    private DireccionDeEnvio direccionEnvio;
    private LocalDateTime fechaEntrega;

    private Compra(String id, String compradorId, LocalDate fechaCompra, DireccionDeEnvio direccionEnvio,
                   List<DetalleDeCompra> detalles, EstadoDeCompra estado, LocalDateTime fechaEntrega) {
        this.id = id;
        this.compradorId = compradorId;
        this.fechaCompra = fechaCompra;
        this.direccionEnvio = direccionEnvio;
        this.detalles = new ArrayList<>(detalles);
        this.estado = estado;
        this.fechaEntrega = fechaEntrega;
    }

    /**
     * Abre el carrito de un Comprador: una Compra PENDIENTE, vacía y aún sin Dirección de Envío.
     *
     * @throws ReglaDominioException si falta el identificador o el comprador
     */
    public static Compra abrirCarrito(String id, String compradorId) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La compra debe tener un identificador");
        }
        if (compradorId == null || compradorId.isBlank()) {
            throw new ReglaDominioException("La compra debe estar asociada a un comprador");
        }
        return new Compra(id, compradorId, LocalDate.now(), null, List.of(), EstadoDeCompra.PENDIENTE, null);
    }

    /**
     * Revive una Compra ya guardada, tal como quedó. No valida: solo la usa la persistencia.
     */
    public static Compra reconstruir(String id, String compradorId, LocalDate fechaCompra,
                                     DireccionDeEnvio direccionEnvio, List<DetalleDeCompra> detalles,
                                     EstadoDeCompra estado, LocalDateTime fechaEntrega) {
        return new Compra(id, compradorId, fechaCompra, direccionEnvio, detalles, estado, fechaEntrega);
    }

    public void agregarDetalle(String presentacionId, Precio precioCongelado, Cantidad cantidad) {
        if (estado != EstadoDeCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se pueden agregar detalles a una compra pendiente");
        }
        detalles.add(new DetalleDeCompra(id, presentacionId, precioCongelado, cantidad));
    }

    /**
     * Confirma el carrito: exige al menos un detalle y fija la Dirección de Envío.
     * Una Compra no puede confirmarse dos veces.
     */
    public void confirmar(DireccionDeEnvio direccionEnvio) {
        if (estado != EstadoDeCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se puede confirmar una compra pendiente (estado actual: " + estado + ")");
        }
        if (detalles.isEmpty()) {
            throw new ReglaDominioException("No se puede confirmar una compra sin detalles");
        }
        if (direccionEnvio == null) {
            throw new ReglaDominioException("Para confirmar la compra se necesita una direccion de envio");
        }
        cambiarEstado(EstadoDeCompra.CONFIRMADA);
        this.direccionEnvio = direccionEnvio;
    }

    public boolean puedeCancelarse() {
        return estado.puedeTransicionarA(EstadoDeCompra.CANCELADA);
    }

    /** Cancela la Compra sin costo mientras no se haya enviado. */
    public void cancelar() {
        if (!puedeCancelarse()) {
            throw new ReglaDominioException("No se puede cancelar una compra en estado " + estado);
        }
        cambiarEstado(EstadoDeCompra.CANCELADA);
    }

    /** Marca como enviada una Compra confirmada. */
    public void enviar() {
        if (estado != EstadoDeCompra.CONFIRMADA) {
            throw new ReglaDominioException("Solo se puede enviar una compra confirmada (estado actual: " + estado + ")");
        }
        cambiarEstado(EstadoDeCompra.ENVIADA);
    }

    /**
     * Marca como entregada una Compra enviada y registra cuándo se entregó
     * (desde ese momento corre el plazo de reembolso).
     */
    public void entregar(LocalDateTime fechaEntrega) {
        if (fechaEntrega == null) {
            throw new ReglaDominioException("La entrega debe registrar la fecha y hora en que ocurrio");
        }
        if (estado != EstadoDeCompra.ENVIADA) {
            throw new ReglaDominioException("Solo se puede entregar una compra enviada (estado actual: " + estado + ")");
        }
        cambiarEstado(EstadoDeCompra.ENTREGADA);
        this.fechaEntrega = fechaEntrega;
    }

    private void cambiarEstado(EstadoDeCompra nuevo) {
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

    /** Indica si la Compra incluye esa Presentación en alguno de sus detalles. */
    public boolean incluye(String presentacionId) {
        return detalles.stream().anyMatch(detalle -> detalle.presentacionId().equals(presentacionId));
    }

    public String getId() {
        return id;
    }

    public String getCompradorId() {
        return compradorId;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    /** Vacía mientras la Compra sea un carrito sin confirmar. */
    public Optional<DireccionDeEnvio> getDireccionEnvio() {
        return Optional.ofNullable(direccionEnvio);
    }

    public List<DetalleDeCompra> getDetalles() {
        return List.copyOf(detalles);
    }

    public EstadoDeCompra getEstado() {
        return estado;
    }

    public Optional<LocalDateTime> getFechaEntrega() {
        return Optional.ofNullable(fechaEntrega);
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
