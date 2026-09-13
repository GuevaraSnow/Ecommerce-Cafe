package com.uniquindio.ecommerce.Domain.valueobject;

/**
 * Clasifica el producto que ofrece una Presentacion,
 * determinando de dónde debe originarse (Lote o Transformación) y qué
 * validaciones adicionales aplican.
 */
public enum TipoProducto {
    /** Café sin procesar, vendido directamente desde un Lote. */
    CAFE_VERDE,
    /** Café en pergamino, vendido directamente desde un Lote. */
    CAFE_PERGAMINO,
    /** Café tostado; requiere provenir de una Transformación y tener fecha de tueste vigente. */
    CAFE_TOSTADO,
    /** Producto derivado del café; requiere provenir de una Transformación. */
    DERIVADO
}
