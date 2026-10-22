package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain Service con las reglas que cruzan Compra y Presentación. El stock se
 * descuenta al confirmar la Compra (no al agregar al carrito), para que un
 * carrito abandonado no bloquee café que otros sí podrían comprar.
 * Sin estado y sin anotaciones de Spring.
 */
public class ServicioDeCompra {

    private final PresentacionRepositorio presentacionRepositorio;

    public ServicioDeCompra(PresentacionRepositorio presentacionRepositorio) {
        this.presentacionRepositorio = presentacionRepositorio;
    }

    /**
     * Agrega una Presentación al carrito congelando su precio vigente (con el Descuento
     * por temporada si rige ese día). Verifica, sin descontar nada, que la Presentación
     * esté a la venta y que su stock alcance para el total que quedaría en el carrito:
     * si ya hay 6 kg y se agregan 6 más con 11 en stock, se rechaza. Al confirmar se
     * vuelve a verificar, por si alguien compró antes.
     *
     * @throws ReglaDominioException si la Presentación no está a la venta, el stock no alcanza
     *                               o la Compra ya no es un carrito; en ese caso el carrito no cambia
     */
    public void agregarAlCarrito(Compra carrito, Presentacion presentacion, Cantidad cantidad, LocalDate hoy) {
        Cantidad totalEnCarrito = carrito.cantidadDe(presentacion.getId())
                .map(enCarrito -> enCarrito.sumar(cantidad))
                .orElse(cantidad);
        presentacion.verificarQueSePuedeVender(totalEnCarrito);
        carrito.agregarPresentacion(presentacion.getId(), presentacion.precioVigente(hoy), cantidad);
    }

    /**
     * Confirma el carrito y descuenta el stock de cada Presentación, todo o nada:
     * primero verifica que todas se pueden vender en la cantidad pedida, después
     * confirma la Compra y solo entonces descuenta y guarda cada Presentación.
     *
     * @throws ReglaDominioException si alguna Presentación no se puede vender en esa cantidad
     *                               o si la Compra no se puede confirmar; en ese caso nada cambia
     */
    public void confirmar(Compra compra, DireccionDeEnvio direccionEnvio) {
        List<DetalleDeCompra> detalles = compra.getDetalles();
        List<Presentacion> presentaciones = detalles.stream()
                .map(detalle -> presentacionRepositorio.buscarPorId(detalle.presentacionId()).orElseThrow())
                .toList();

        for (int i = 0; i < detalles.size(); i++) {
            presentaciones.get(i).verificarQueSePuedeVender(detalles.get(i).cantidad());
        }

        compra.confirmar(direccionEnvio);

        for (int i = 0; i < detalles.size(); i++) {
            presentaciones.get(i).descontarCantidad(detalles.get(i).cantidad());
            presentacionRepositorio.guardar(presentaciones.get(i));
        }
    }

    /**
     * Cancela la Compra y, si ya estaba confirmada (el stock se había descontado),
     * repone el stock de cada Presentación. Un carrito pendiente nunca descontó stock,
     * así que solo se cancela. Una Presentación dada de baja no recibe stock de vuelta.
     *
     * @throws ReglaDominioException si la Compra ya no se puede cancelar; en ese caso nada cambia
     */
    public void cancelar(Compra compra) {
        boolean habiaDescontadoStock = compra.getEstado() == EstadoDeCompra.CONFIRMADA;
        List<DetalleDeCompra> detalles = compra.getDetalles();
        List<Presentacion> presentaciones = detalles.stream()
                .map(detalle -> presentacionRepositorio.buscarPorId(detalle.presentacionId()).orElseThrow())
                .toList();

        compra.cancelar();

        for (int i = 0; i < detalles.size(); i++) {
            Presentacion presentacion = presentaciones.get(i);
            if (habiaDescontadoStock && !presentacion.isEliminada()) {
                presentacion.reponerCantidad(detalles.get(i).cantidad());
                presentacionRepositorio.guardar(presentacion);
            }
        }
    }
}
