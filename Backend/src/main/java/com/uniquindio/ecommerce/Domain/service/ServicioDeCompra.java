package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;

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
