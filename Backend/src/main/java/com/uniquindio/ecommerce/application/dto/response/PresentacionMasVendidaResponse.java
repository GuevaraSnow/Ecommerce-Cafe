package com.uniquindio.ecommerce.application.dto.response;

// Una fila del ranking de las Presentaciones más vendidas: en cuántas Compras está y cuánto se recaudó con ella.
public record PresentacionMasVendidaResponse(String presentacionId, String titulo, long compras, double ingresos) {}
