package com.uniquindio.ecommerce.Domain.valueobject;

import java.util.EnumSet;
import java.util.Set;

public enum EstadoDeCompra {
    PENDIENTE,
    CONFIRMADA,
    ENVIADA,
    ENTREGADA,
    CANCELADA,
    REEMBOLSADA;

    /** Los estados en que una Compra cuenta como venta: ya se confirmó y no se canceló ni se reembolsó. */
    public static final Set<EstadoDeCompra> VENTAS = EnumSet.of(CONFIRMADA, ENVIADA, ENTREGADA);

    public boolean puedeTransicionarA(EstadoDeCompra destino) {
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADA || destino == CANCELADA;
            case CONFIRMADA -> destino == ENVIADA || destino == CANCELADA;
            case ENVIADA -> destino == ENTREGADA;
            case ENTREGADA -> destino == REEMBOLSADA;
            case CANCELADA, REEMBOLSADA -> false;
        };
    }
}