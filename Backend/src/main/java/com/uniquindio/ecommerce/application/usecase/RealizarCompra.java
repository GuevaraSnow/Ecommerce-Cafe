package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * descuenta la cantidad disponible. Si una Presentacion se pide varias
     * veces queda en un solo detalle con la cantidad sumada, y es esa suma la
     * que se verifica. Todo o nada: si algun item no se puede comprar, no se
     * descuenta el stock de ninguno.
     */
    public Compra ejecutar(String compradorId, List<ItemSolicitado> items) {
        if (items == null || items.isEmpty()) {
            throw new ReglaDominioException("La compra debe tener al menos un item");
        }

        Compra compra = Compra.abrirCarrito(UUID.randomUUID().toString(), compradorId);
        LocalDate hoy = LocalDate.now();

        // Primera pasada: armar el carrito; las Presentaciones repetidas quedan en un solo detalle
        Map<String, Presentacion> presentaciones = new HashMap<>();
        for (ItemSolicitado item : items) {
            Presentacion presentacion = repositorioPresentacion.buscarPorId(item.presentacionId())
                    .orElseThrow(() -> new ReglaDominioException("Presentacion no encontrada: " + item.presentacionId()));
            compra.agregarPresentacion(item.presentacionId(), presentacion.precioVigente(hoy), item.cantidad());
            presentaciones.put(item.presentacionId(), presentacion);
        }

        // Segunda pasada: verificar la cantidad ya sumada de cada detalle SIN modificar nada
        for (DetalleDeCompra detalle : compra.getDetalles()) {
            presentaciones.get(detalle.presentacionId()).verificarQueSePuedeVender(detalle.cantidad());
        }

        // Tercera pasada: ya es seguro descontar el stock
        for (DetalleDeCompra detalle : compra.getDetalles()) {
            Presentacion presentacion = presentaciones.get(detalle.presentacionId());
            presentacion.descontarCantidad(detalle.cantidad());
            repositorioPresentacion.guardar(presentacion);
        }

        repositorioCompra.guardar(compra);
        return compra;
    }
}