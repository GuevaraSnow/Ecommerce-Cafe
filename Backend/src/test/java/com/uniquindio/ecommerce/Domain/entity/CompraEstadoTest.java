package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompraEstadoTest {

    private CompraFisica compraConDetalle() {
        CompraFisica compra = CompraFisica.iniciar("comprador-1",
                new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        compra.agregarDetalle("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        return compra;
    }

    private CompraFisica compraEnviada() {
        CompraFisica compra = compraConDetalle();
        compra.confirmar();
        compra.marcarEnviada();
        return compra;
    }

    @Test
    void confirmarPasaLaCompraDePendienteAConfirmada() {
        CompraFisica compra = compraConDetalle();

        compra.confirmar();

        assertEquals(EstadoDeCompra.CONFIRMADA, compra.getEstado());
    }

    @Test
    void unaMismaCompraNoPuedeConfirmarseDosVeces() {
        CompraFisica compra = compraConDetalle();
        compra.confirmar();

        assertThrows(ReglaDominioException.class, compra::confirmar);
        assertEquals(EstadoDeCompra.CONFIRMADA, compra.getEstado());
    }

    @Test
    void noSePuedeConfirmarUnaCompraSinDetalles() {
        CompraFisica vacia = CompraFisica.iniciar("comprador-1",
                new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));

        assertThrows(ReglaDominioException.class, vacia::confirmar);
        assertEquals(EstadoDeCompra.PENDIENTE, vacia.getEstado());
    }

    @Test
    void despuesDeConfirmadaYaNoSePuedenAgregarDetalles() {
        CompraFisica compra = compraConDetalle();
        compra.confirmar();

        assertThrows(ReglaDominioException.class,
                () -> compra.agregarDetalle("presentacion-2", new Precio(30000, "COP"), new Cantidad(1, "kg")));
        assertEquals(1, compra.getDetalles().size());
    }

    @Test
    void sePuedeCancelarUnaCompraPendiente() {
        CompraFisica compra = compraConDetalle();

        compra.cancelar();

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void sePuedeCancelarUnaCompraConfirmadaMientrasNoSeHayaEnviado() {
        CompraFisica compra = compraConDetalle();
        compra.confirmar();

        assertTrue(compra.puedeCancelarse());
        compra.cancelar();

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void noSePuedeCancelarUnaCompraYaEnviada() {
        CompraFisica compra = compraEnviada();

        assertFalse(compra.puedeCancelarse());
        assertThrows(ReglaDominioException.class, compra::cancelar);
        assertEquals(EstadoDeCompra.ENVIADA, compra.getEstado());
    }

    @Test
    void noSePuedeCancelarDosVeces() {
        CompraFisica compra = compraConDetalle();
        compra.cancelar();

        assertThrows(ReglaDominioException.class, compra::cancelar);
    }

    @Test
    void soloSePuedeEnviarUnaCompraConfirmada() {
        CompraFisica pendiente = compraConDetalle();

        assertThrows(ReglaDominioException.class, pendiente::marcarEnviada);
        assertEquals(EstadoDeCompra.PENDIENTE, pendiente.getEstado());
    }

    @Test
    void entregarUnaCompraEnviadaRegistraLaFechaDeEntrega() {
        CompraFisica compra = compraEnviada();

        compra.marcarEntregada();

        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
        assertTrue(compra.getFechaEntrega().isPresent());
    }

    @Test
    void noSePuedeEntregarUnaCompraQueNoFueEnviada() {
        CompraFisica compra = compraConDetalle();
        compra.confirmar();

        assertThrows(ReglaDominioException.class, compra::marcarEntregada);
        assertTrue(compra.getFechaEntrega().isEmpty());
    }

    @Test
    void unaCompraEntregadaYaNoSePuedeCancelar() {
        CompraFisica compra = compraEnviada();
        compra.marcarEntregada();

        assertThrows(ReglaDominioException.class, compra::cancelar);
    }
}