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

    /**
     * Agrega una Presentación al carrito congelando su precio. Si ya estaba en el
     * carrito, suma la cantidad y conserva el precio que se congeló la primera vez.
     *
     * @throws ReglaDominioException si la Compra ya no es un carrito, si el detalle es
     *                               inválido o si la unidad no coincide con la que ya tenía
     */
    public void agregarPresentacion(String presentacionId, Precio precioCongelado, Cantidad cantidad) {
        validarQueEsCarrito();
        DetalleDeCompra nuevo = new DetalleDeCompra(id, presentacionId, precioCongelado, cantidad);
        Optional<DetalleDeCompra> existente = buscarDetalle(presentacionId);
        if (existente.isEmpty()) {
            detalles.add(nuevo);
            return;
        }
        DetalleDeCompra actual = existente.get();
        DetalleDeCompra sumado = new DetalleDeCompra(id, presentacionId, actual.precioCongelado(),
                actual.cantidad().sumar(cantidad));
        detalles.set(detalles.indexOf(actual), sumado);
    }

    /**
     * Cambia la cantidad de una Presentación que ya está en el carrito. El precio
     * congelado no cambia y la unidad debe ser la misma; para sacarla se usa
     * {@link #quitarPresentacion(String)}.
     *
     * @throws ReglaDominioException si la Compra ya no es un carrito, si la Presentación
     *                               no está en él o si la cantidad no es válida
     */
    public void cambiarCantidad(String presentacionId, Cantidad nuevaCantidad) {
        validarQueEsCarrito();
        DetalleDeCompra actual = detalleEnCarrito(presentacionId);
        if (nuevaCantidad != null && !nuevaCantidad.unidad().equals(actual.cantidad().unidad())) {
            throw new ReglaDominioException("La cantidad debe expresarse en " + actual.cantidad().unidad());
        }
        DetalleDeCompra cambiado = new DetalleDeCompra(id, presentacionId, actual.precioCongelado(), nuevaCantidad);
        detalles.set(detalles.indexOf(actual), cambiado);
    }

    /**
     * Saca una Presentación del carrito.
     *
     * @throws ReglaDominioException si la Compra ya no es un carrito o si la Presentación no está en él
     */
    public void quitarPresentacion(String presentacionId) {
        validarQueEsCarrito();
        detalles.remove(detalleEnCarrito(presentacionId));
    }

    /** Cantidad de esa Presentación que ya hay en la Compra; vacía si no está. */
    public Optional<Cantidad> cantidadDe(String presentacionId) {
        return buscarDetalle(presentacionId).map(DetalleDeCompra::cantidad);
    }

    private DetalleDeCompra detalleEnCarrito(String presentacionId) {
        return buscarDetalle(presentacionId).orElseThrow(
                () -> new ReglaDominioException("La Presentacion " + presentacionId + " no esta en el carrito"));
    }

    private Optional<DetalleDeCompra> buscarDetalle(String presentacionId) {
        return detalles.stream().filter(detalle -> detalle.presentacionId().equals(presentacionId)).findFirst();
    }

    private void validarQueEsCarrito() {
        if (estado != EstadoDeCompra.PENDIENTE) {
            throw new ReglaDominioException("Solo se puede modificar el carrito de una compra pendiente (estado actual: "
                    + estado + ")");
        }
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
