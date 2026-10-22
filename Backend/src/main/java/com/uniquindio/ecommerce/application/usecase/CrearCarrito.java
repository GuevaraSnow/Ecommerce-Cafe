package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: CrearCarrito.
 * (Actor: Comprador) — abre un carrito vacío: una Compra PENDIENTE sin Presentaciones
 * ni Dirección de Envío. No tiene lógica: delega en Compra.abrirCarrito().
 */
@Service
@RequiredArgsConstructor
public class CrearCarrito {

    private final CompraRepositorio compraRepositorio;

    @Transactional
    public Compra ejecutar(String id, String compradorId) {
        Compra carrito = Compra.abrirCarrito(id, compradorId);
        compraRepositorio.guardar(carrito);
        return carrito;
    }
}
