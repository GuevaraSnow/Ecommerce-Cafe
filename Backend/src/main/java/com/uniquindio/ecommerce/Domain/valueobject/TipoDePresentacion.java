package com.uniquindio.ecommerce.Domain.valueobject;

/**
 * Clasifica qué es una Presentación, determinando de dónde debe originarse
 * (Lote o Transformación) y qué validaciones adicionales aplican.
 */
public enum TipoDePresentacion {
    /** Café sin procesar, vendido directamente desde un Lote. */
    CAFE_VERDE,
    /** Café en pergamino, vendido directamente desde un Lote. */
    CAFE_PERGAMINO,
    /** Café tostado; requiere provenir de una Transformación y tener fecha de tueste vigente. */
    CAFE_TOSTADO,
    /** Derivado del café (miel, licor, cosmético, merchandising); requiere provenir de una Transformación. */
    DERIVADO
}
