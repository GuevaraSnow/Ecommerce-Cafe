package com.uniquindio.ecommerce.Domain.repository;

/**
 * Una fila del ranking de ventas: cuántas Compras incluyen la Presentación y cuánto se recaudó con ella
 * (precio congelado por cantidad). No suma cantidades porque mezclan unidades (kg y unidades).
 */
public record PresentacionVendida(String presentacionId, String titulo, long compras, double ingresos) {
}
