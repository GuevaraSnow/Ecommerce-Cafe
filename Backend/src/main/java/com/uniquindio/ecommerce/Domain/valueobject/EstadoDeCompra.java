package com.uniquindio.ecommerce.Domain.valueobject;

public enum EstadoDeCompra {
    PENDIENTE,
    CONFIRMADA,
    ENVIADA,
    ENTREGADA,
    CANCELADA,
    REEMBOLSADA;

    /** Flujo valido de una Compra; CANCELADA y REEMBOLSADA son estados finales. */
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