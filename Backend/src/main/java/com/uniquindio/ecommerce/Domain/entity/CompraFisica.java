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
import java.util.Optional;

/**
 * Compra de Presentaciones físicas: lleva Dirección de Envío, Detalles por
 * Presentación y el tramo logístico (Enviada, Entregada).
 */
public class CompraFisica extends Compra {

    private final List<DetalleDeCompra> detalles;
    private final DireccionDeEnvio direccionEnvio;
    private LocalDate fechaEntrega;

    private CompraFisica(String compradorId, DireccionDeEnvio direccionEnvio) {
        super(compradorId);
        this.detalles = new ArrayList<>();
        this.direccionEnvio = direccionEnvio;
        this.fechaEntrega = null;
    }

    public static CompraFisica iniciar(String compradorId, DireccionDeEnvio direccionEnvio) {
        if (direccionEnvio == null) {
            throw new ReglaDominioException("La compra debe tener una direccion de envio");
        }
        return new CompraFisica(compradorId, direccionEnvio);
    }

    public void agregarDetalle(String presentacionId, Precio precioCongelado, Cantidad cantidad) {
        if (getEstado() != EstadoDeCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se pueden agregar detalles a una compra pendiente");
        }
        detalles.add(new DetalleDeCompra(getId(), presentacionId, precioCongelado, cantidad));
    }

    @Override
    protected void validarConfirmable() {
        if (detalles.isEmpty()) {
            throw new ReglaDominioException("No se puede confirmar una compra sin detalles");
        }
    }

    public void marcarEnviada() {
        if (getEstado() != EstadoDeCompra.CONFIRMADA) {
            throw new ReglaDominioException("Solo se puede enviar una compra confirmada (estado actual: " + getEstado() + ")");
        }
        cambiarEstado(EstadoDeCompra.ENVIADA);
    }

    public void marcarEntregada() {
        if (getEstado() != EstadoDeCompra.ENVIADA) {
            throw new ReglaDominioException("Solo se puede entregar una compra enviada (estado actual: " + getEstado() + ")");
        }
        cambiarEstado(EstadoDeCompra.ENTREGADA);
        this.fechaEntrega = LocalDate.now();
    }

    @Override
    public boolean incluye(String presentacionId) {
        return detalles.stream().anyMatch(detalle -> detalle.presentacionId().equals(presentacionId));
    }

    public List<DetalleDeCompra> getDetalles() {
        return List.copyOf(detalles);
    }

    public DireccionDeEnvio getDireccionEnvio() {
        return direccionEnvio;
    }

    public Optional<LocalDate> getFechaEntrega() {
        return Optional.ofNullable(fechaEntrega);
    }
}
