package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
     * congela el precio vigente de cada una (con el Descuento por temporada
     * si rige hoy) en su DetalleDeCompra, y
     * descuenta la cantidad disponible. Todo o nada: si algun item no se
     * puede comprar, no se descuenta el stock de ninguno.
     */
    public Compra ejecutar(String compradorId, List<ItemSolicitado> items) {
        if (items == null || items.isEmpty()) {
            throw new ReglaDominioException("La compra debe tener al menos un item");
        }

        Compra compra = Compra.abrirCarrito(UUID.randomUUID().toString(), compradorId);
        LocalDate hoy = LocalDate.now();

        // Primera pasada: validar todo SIN modificar nada
        List<Presentacion> presentaciones = new ArrayList<>();
        for (ItemSolicitado item : items) {
            Presentacion presentacion = repositorioPresentacion.buscarPorId(item.presentacionId())
                    .orElseThrow(() -> new ReglaDominioException("Presentacion no encontrada: " + item.presentacionId()));
            presentacion.verificarQueSePuedeVender(item.cantidad());
            presentaciones.add(presentacion);
        }

        // Segunda pasada: ya es seguro modificar
        for (int i = 0; i < items.size(); i++) {
            ItemSolicitado item = items.get(i);
            Presentacion presentacion = presentaciones.get(i);
            presentacion.descontarCantidad(item.cantidad());
            compra.agregarPresentacion(item.presentacionId(), presentacion.precioVigente(hoy), item.cantidad());
            repositorioPresentacion.guardar(presentacion);
        }

        repositorioCompra.guardar(compra);
        return compra;
    }
}