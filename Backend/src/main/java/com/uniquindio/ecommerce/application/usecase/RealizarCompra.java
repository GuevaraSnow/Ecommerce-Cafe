package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;

import java.util.ArrayList;
import java.util.List;

public class RealizarCompra {

    private final PresentacionRepositorio repositorioPresentacion;
    private final CompraRepositorio repositorioCompra;

    public RealizarCompra(PresentacionRepositorio repositorioPresentacion, CompraRepositorio repositorioCompra) {
        this.repositorioPresentacion = repositorioPresentacion;
        this.repositorioCompra = repositorioCompra;
    }

    /** Un item del carrito: que Presentacion y cuanta Cantidad. */
    public record ItemSolicitado(String presentacionId, Cantidad cantidad) {}

    /**
     * Ejecuta la compra de una o varias Presentaciones: valida stock,
     * congela el precio actual de cada una en su DetalleDeCompra, y
     * descuenta la cantidad disponible. Todo o nada: si algun item no se
     * puede comprar, no se descuenta el stock de ninguno.
     */
    public Compra ejecutar(String compradorId, List<ItemSolicitado> items,
                           String ciudad, String direccion, String destinatario) {
        if (items == null || items.isEmpty()) {
            throw new ReglaDominioException("La compra debe tener al menos un item");
        }

        DireccionDeEnvio direccionEnvio = new DireccionDeEnvio(ciudad, direccion, destinatario);
        Compra compra = Compra.iniciar(compradorId, direccionEnvio);

        // Primera pasada: validar todo SIN modificar nada
        List<Presentacion> presentaciones = new ArrayList<>();
        for (ItemSolicitado item : items) {
            Presentacion presentacion = repositorioPresentacion.buscarPorId(item.presentacionId())
                    .orElseThrow(() -> new ReglaDominioException("Presentacion no encontrada: " + item.presentacionId()));
            if (presentacion.isEliminada()) {
                throw new ReglaDominioException("La Presentacion fue dada de baja: " + item.presentacionId());
            }
            // restar() devuelve una Cantidad nueva (no modifica nada) y lanza si no alcanza el stock
            presentacion.getCantidadDisponible().restar(item.cantidad());
            presentaciones.add(presentacion);
        }

        // Segunda pasada: ya es seguro modificar
        for (int i = 0; i < items.size(); i++) {
            ItemSolicitado item = items.get(i);
            Presentacion presentacion = presentaciones.get(i);
            presentacion.descontarCantidad(item.cantidad());
            compra.agregarDetalle(item.presentacionId(), presentacion.getPrecio(), item.cantidad());
            repositorioPresentacion.guardar(presentacion);
        }

        repositorioCompra.guardar(compra);
        return compra;
    }
}