package com.uniquindio.ecommerce.application.dto.response;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Una Compra (o el carrito, si está PENDIENTE). La dirección de envío y la fecha de entrega
 * vienen en null mientras no existan: un carrito no tiene dirección y una compra no entregada
 * no tiene fecha de entrega.
 */
public record CompraResponse(
        String id,
        String compradorId,
        EstadoDeCompra estado,
        LocalDate fechaCompra,
        List<DetalleDeCompraResponse> detalles,
        DireccionDeEnvioResponse direccionEnvio,
        LocalDateTime fechaEntrega
) {}
