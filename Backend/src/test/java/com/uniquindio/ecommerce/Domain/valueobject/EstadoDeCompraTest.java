package com.uniquindio.ecommerce.Domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstadoDeCompraTest {

    private void assertSoloPuedePasarA(EstadoDeCompra origen, EstadoDeCompra... permitidos) {
        List<EstadoDeCompra> lista = List.of(permitidos);
        for (EstadoDeCompra destino : EstadoDeCompra.values()) {
            assertEquals(lista.contains(destino), origen.puedeTransicionarA(destino), origen + " -> " + destino);
        }
    }

    @Test
    void unaCompraPendienteSoloPuedeConfirmarseOCancelarse() {
        assertSoloPuedePasarA(EstadoDeCompra.PENDIENTE, EstadoDeCompra.CONFIRMADA, EstadoDeCompra.CANCELADA);
    }

    @Test
    void unaCompraConfirmadaSoloPuedeEnviarseOCancelarse() {
        assertSoloPuedePasarA(EstadoDeCompra.CONFIRMADA, EstadoDeCompra.ENVIADA, EstadoDeCompra.CANCELADA);
    }

    @Test
    void unaCompraEnviadaSoloPuedeEntregarse() {
        assertSoloPuedePasarA(EstadoDeCompra.ENVIADA, EstadoDeCompra.ENTREGADA);
    }

    @Test
    void unaCompraEntregadaSoloPuedeReembolsarse() {
        assertSoloPuedePasarA(EstadoDeCompra.ENTREGADA, EstadoDeCompra.REEMBOLSADA);
    }

    @Test
    void canceladaYReembolsadaSonEstadosFinales() {
        assertSoloPuedePasarA(EstadoDeCompra.CANCELADA);
        assertSoloPuedePasarA(EstadoDeCompra.REEMBOLSADA);
    }
}