package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Compra {

    private final String id;
    private final String compradorId;
    private final List<DetalleDeCompra> detalles;
    private EstadoDeCompra estado;
    private final DireccionDeEnvio direccionEnvio;
    private final LocalDate fechaCompra;
    private LocalDate fechaEntrega;

    private Compra(String compradorId, DireccionDeEnvio direccionEnvio) {
        this.id = UUID.randomUUID().toString();
        this.compradorId = compradorId;
        this.detalles = new ArrayList<>();
        this.estado = EstadoDeCompra.PENDIENTE;
        this.direccionEnvio = direccionEnvio;
        this.fechaCompra = LocalDate.now();
        this.fechaEntrega = null;
    }

    public static Compra iniciar(String compradorId, DireccionDeEnvio direccionEnvio) {
        if (compradorId == null || compradorId.isBlank()) {
            throw new ReglaDominioException("La compra debe estar asociada a un comprador");
        }
        if (direccionEnvio == null) {
            throw new ReglaDominioException("La compra debe tener una direccion de envio");
        }
        return new Compra(compradorId, direccionEnvio);
    }

    public void agregarDetalle(String presentacionId, Precio precioCongelado, Cantidad cantidad) {
        if (estado != EstadoDeCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se pueden agregar detalles a una compra pendiente");
        }
        detalles.add(new DetalleDeCompra(id, presentacionId, precioCongelado, cantidad));
    }

    /** Confirma la compra. Solo se confirma una vez, desde PENDIENTE y con al menos un detalle. */
    public void confirmar() {
        if (estado != EstadoDeCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se puede confirmar una compra pendiente (estado actual: " + estado + ")");
        }
        if (detalles.isEmpty()) {
            throw new ReglaDominioException("No se puede confirmar una compra sin detalles");
        }
        cambiarEstado(EstadoDeCompra.CONFIRMADA);
    }

    public boolean puedeCancelarse() {
        return estado.puedeTransicionarA(EstadoDeCompra.CANCELADA);
    }

    /** Cancela la compra mientras no haya sido enviada. */
    public void cancelar() {
        if (!puedeCancelarse()) {
            throw new ReglaDominioException("No se puede cancelar una compra en estado " + estado);
        }
        cambiarEstado(EstadoDeCompra.CANCELADA);
    }

    public void marcarEnviada() {
        if (estado != EstadoDeCompra.CONFIRMADA) {
            throw new ReglaDominioException("Solo se puede enviar una compra confirmada (estado actual: " + estado + ")");
        }
        cambiarEstado(EstadoDeCompra.ENVIADA);
    }

    public void marcarEntregada() {
        if (estado != EstadoDeCompra.ENVIADA) {
            throw new ReglaDominioException("Solo se puede entregar una compra enviada (estado actual: " + estado + ")");
        }
        cambiarEstado(EstadoDeCompra.ENTREGADA);
        this.fechaEntrega = LocalDate.now();
    }

    private void cambiarEstado(EstadoDeCompra nuevo) {
        if (!estado.puedeTransicionarA(nuevo)) {
            throw new ReglaDominioException("Transicion de estado invalida: " + estado + " -> " + nuevo);
        }
        this.estado = nuevo;
    }

    public String getId() {
        return id;
    }

    public String getCompradorId() {
        return compradorId;
    }

    public List<DetalleDeCompra> getDetalles() {
        return List.copyOf(detalles);
    }

    public EstadoDeCompra getEstado() {
        return estado;
    }

    public DireccionDeEnvio getDireccionEnvio() {
        return direccionEnvio;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public Optional<LocalDate> getFechaEntrega() {
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