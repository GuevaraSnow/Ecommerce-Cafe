package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;

import java.util.ArrayList;
import java.util.List;

public class CancelarCompra {

    private final CompraRepositorio repositorioCompra;
    private final PresentacionRepositorio repositorioPresentacion;

    public CancelarCompra(CompraRepositorio repositorioCompra, PresentacionRepositorio repositorioPresentacion) {
        this.repositorioCompra = repositorioCompra;
        this.repositorioPresentacion = repositorioPresentacion;
    }

    /**
     * Cancela una Compra que aun no fue enviada y devuelve el stock de cada
     * Presentacion (RealizarCompra lo descuenta al crear la compra). Todo o
     * nada: si algo falla, ni la compra ni el stock cambian. Las
     * Presentaciones dadas de baja no reciben stock de vuelta.
     */
    public Compra ejecutar(String compraId) {
        Compra compra = repositorioCompra.buscarPorId(compraId)
                .orElseThrow(() -> new ReglaDominioException("Compra no encontrada: " + compraId));


        if (!compra.puedeCancelarse()) {
            throw new ReglaDominioException("No se puede cancelar una compra en estado " + compra.getEstado());
        }
        List<Presentacion> presentaciones = new ArrayList<>();
        for (DetalleDeCompra detalle : compra.getDetalles()) {
            presentaciones.add(repositorioPresentacion.buscarPorId(detalle.presentacionId())
                    .orElseThrow(() -> new ReglaDominioException(
                            "Presentacion no encontrada: " + detalle.presentacionId())));
        }


        compra.cancelar();
        List<DetalleDeCompra> detalles = compra.getDetalles();
        for (int i = 0; i < detalles.size(); i++) {
            Presentacion presentacion = presentaciones.get(i);
            if (!presentacion.isEliminada()) {
                presentacion.reponerCantidad(detalles.get(i).cantidad());
                repositorioPresentacion.guardar(presentacion);
            }
        }
        repositorioCompra.guardar(compra);
        return compra;
    }
}