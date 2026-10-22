package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Caso de uso: AgregarAlCarrito.
 * (Actor: Comprador) — agrega una Presentación a su carrito. No tiene lógica:
 * {@link ServicioDeCompra} verifica que esté a la venta y que el stock alcance para el
 * total del carrito, y congela su precio vigente. El stock no se descuenta aquí, sino al confirmar.
 */
@Service
@RequiredArgsConstructor
public class AgregarAlCarrito {

    private final CompraRepositorio compraRepositorio;
    private final PresentacionRepositorio presentacionRepositorio;
    private final ServicioDeCompra servicioDeCompra;

    @Transactional
    public Compra ejecutar(String compraId, String presentacionId, Cantidad cantidad) {
        Compra carrito = compraRepositorio.buscarPorId(compraId).orElseThrow();
        Presentacion presentacion = presentacionRepositorio.buscarPorId(presentacionId).orElseThrow();
        servicioDeCompra.agregarAlCarrito(carrito, presentacion, cantidad, LocalDate.now());
        compraRepositorio.guardar(carrito);
        return carrito;
    }
}
