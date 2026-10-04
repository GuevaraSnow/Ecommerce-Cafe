package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompraDigitalTest {

    private CompraDigital compraValida() {
        return CompraDigital.iniciar("comprador-1", "curso-1", new Precio(50000, "COP"));
    }

    @Test
    void unaCompraDigitalRecienIniciadaEstaPendienteYGuardaElCursoYElPrecio() {
        CompraDigital compra = compraValida();

        assertEquals(EstadoDeCompra.PENDIENTE, compra.getEstado());
        assertEquals("curso-1", compra.getCursoId());
        assertEquals(new Precio(50000, "COP"), compra.getPrecioCongelado());
        assertEquals("comprador-1", compra.getCompradorId());
    }

    @Test
    void noSePuedeIniciarSinComprador() {
        assertThrows(ReglaDominioException.class,
                () -> CompraDigital.iniciar("  ", "curso-1", new Precio(50000, "COP")));
    }

    @Test
    void noSePuedeIniciarSinCurso() {
        assertThrows(ReglaDominioException.class,
                () -> CompraDigital.iniciar("comprador-1", "  ", new Precio(50000, "COP")));
    }

    @Test
    void noSePuedeIniciarSinPrecio() {
        assertThrows(ReglaDominioException.class, () -> CompraDigital.iniciar("comprador-1", "curso-1", null));
    }

    @Test
    void sePuedeConfirmarSinDetallesPorqueLaCompraEsDeUnSoloCurso() {
        CompraDigital compra = compraValida();

        compra.confirmar();

        assertEquals(EstadoDeCompra.CONFIRMADA, compra.getEstado());
    }

    @Test
    void noSePuedeConfirmarDosVeces() {
        CompraDigital compra = compraValida();
        compra.confirmar();

        assertThrows(ReglaDominioException.class, compra::confirmar);
    }

    @Test
    void sePuedeCancelarMientrasEstaPendiente() {
        CompraDigital compra = compraValida();

        assertTrue(compra.puedeCancelarse());
        compra.cancelar();

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void unaVezConfirmadaYaNoSePuedeCancelar() {
        CompraDigital compra = compraValida();
        compra.confirmar();

        assertFalse(compra.puedeCancelarse());
        assertThrows(ReglaDominioException.class, compra::cancelar);
        assertEquals(EstadoDeCompra.CONFIRMADA, compra.getEstado());
    }

    @Test
    void estaActivaMientrasNoSeCancele() {
        CompraDigital pendiente = compraValida();
        CompraDigital confirmada = compraValida();
        confirmada.confirmar();
        CompraDigital cancelada = compraValida();
        cancelada.cancelar();

        assertTrue(pendiente.estaActiva());
        assertTrue(confirmada.estaActiva());
        assertFalse(cancelada.estaActiva());
    }

    @Test
    void unaCompraDigitalNuncaIncluyePresentaciones() {
        assertFalse(compraValida().incluye("curso-1"));
        assertFalse(compraValida().incluye("cualquier-presentacion"));
    }

    @Test
    void dosComprasDigitalesConLosMismosDatosNoSonLaMisma() {
        assertNotEquals(compraValida(), compraValida());
    }
}
