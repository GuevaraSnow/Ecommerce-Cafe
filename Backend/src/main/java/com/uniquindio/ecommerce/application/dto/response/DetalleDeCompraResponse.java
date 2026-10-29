package com.uniquindio.ecommerce.application.dto.response;

/** Una línea de la Compra: la Presentación, el precio que quedó congelado y la cantidad. */
public record DetalleDeCompraResponse(String presentacionId, PrecioResponse precioCongelado, CantidadResponse cantidad) {}
