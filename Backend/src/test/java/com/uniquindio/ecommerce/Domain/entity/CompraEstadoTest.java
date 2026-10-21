package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CompraEstadoTest {

    private static final DireccionDeEnvio DIRECCION = new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez");
    private static final LocalDateTime FECHA_DE_ENTREGA = LocalDateTime.of(2026, 10, 20, 15, 30);

    private Compra compraConDetalle() {
        Compra compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        return compra;
    }

    private Compra compraEnviada() {
        Compra compra = compraConDetalle();
        compra.confirmar(DIRECCION);
        compra.enviar();
        return compra;
    }

    @Test
    void confirmarPasaLaCompraDePendienteAConfirmada() {
        Compra compra = compraConDetalle();

        compra.confirmar(DIRECCION);

        assertEquals(EstadoDeCompra.CONFIRMADA, compra.getEstado());
    }

    @Test
    void unaMismaCompraNoPuedeConfirmarseDosVeces() {
        Compra compra = compraConDetalle();
        compra.confirmar(DIRECCION);

        assertThrows(ReglaDominioException.class, () -> compra.confirmar(DIRECCION));
        assertEquals(EstadoDeCompra.CONFIRMADA, compra.getEstado());
    }

    @Test
    void noSePuedeConfirmarUnaCompraSinDetalles() {
        Compra vacia = Compra.abrirCarrito("compra-vacia", "comprador-1");

        assertThrows(ReglaDominioException.class, () -> vacia.confirmar(DIRECCION));
        assertEquals(EstadoDeCompra.PENDIENTE, vacia.getEstado());
    }

    @Test
    void despuesDeConfirmadaYaNoSePuedenAgregarPresentaciones() {
        Compra compra = compraConDetalle();
        compra.confirmar(DIRECCION);

        assertThrows(ReglaDominioException.class,
                () -> compra.agregarPresentacion("presentacion-2", new Precio(30000, "COP"), new Cantidad(1, "kg")));
        assertEquals(1, compra.getDetalles().size());
    }

    @Test
    void sePuedeCancelarUnaCompraPendiente() {
        Compra compra = compraConDetalle();

        compra.cancelar();

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void sePuedeCancelarUnaCompraConfirmadaMientrasNoSeHayaEnviado() {
        Compra compra = compraConDetalle();
        compra.confirmar(DIRECCION);

        assertTrue(compra.puedeCancelarse());
        compra.cancelar();

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
    }

    @Test
    void noSePuedeCancelarUnaCompraYaEnviada() {
        Compra compra = compraEnviada();

        assertFalse(compra.puedeCancelarse());
        assertThrows(ReglaDominioException.class, compra::cancelar);
        assertEquals(EstadoDeCompra.ENVIADA, compra.getEstado());
    }

    @Test
    void noSePuedeCancelarDosVeces() {
        Compra compra = compraConDetalle();
        compra.cancelar();

        assertThrows(ReglaDominioException.class, compra::cancelar);
    }

    @Test
    void soloSePuedeEnviarUnaCompraConfirmada() {
        Compra pendiente = compraConDetalle();

        assertThrows(ReglaDominioException.class, pendiente::enviar);
        assertEquals(EstadoDeCompra.PENDIENTE, pendiente.getEstado());
    }

    @Test
    void entregarUnaCompraEnviadaRegistraLaFechaDeEntrega() {
        Compra compra = compraEnviada();

        compra.entregar(FECHA_DE_ENTREGA);

        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
        assertTrue(compra.getFechaEntrega().isPresent());
    }

    @Test
    void noSePuedeEntregarUnaCompraQueNoFueEnviada() {
        Compra compra = compraConDetalle();
        compra.confirmar(DIRECCION);

        assertThrows(ReglaDominioException.class, () -> compra.entregar(FECHA_DE_ENTREGA));
        assertTrue(compra.getFechaEntrega().isEmpty());
    }

    @Test
    void unaCompraEntregadaYaNoSePuedeCancelar() {
        Compra compra = compraEnviada();
        compra.entregar(FECHA_DE_ENTREGA);

        assertThrows(ReglaDominioException.class, compra::cancelar);
    }

    @Test
    void entregarRegistraLaFechaYHoraIndicada() {
        Compra compra = compraEnviada();

        compra.entregar(FECHA_DE_ENTREGA);

        assertEquals(FECHA_DE_ENTREGA, compra.getFechaEntrega().orElseThrow());
    }

    @Test
    void noSePuedeEntregarSinIndicarCuandoYLaCompraSigueEnviada() {
        Compra compra = compraEnviada();

        assertThrows(ReglaDominioException.class, () -> compra.entregar(null));
        assertEquals(EstadoDeCompra.ENVIADA, compra.getEstado());
        assertTrue(compra.getFechaEntrega().isEmpty());
    }

    @Test
    void confirmarFijaLaDireccionDeEnvio() {
        Compra compra = compraConDetalle();

        compra.confirmar(DIRECCION);

        assertEquals(DIRECCION, compra.getDireccionEnvio().orElseThrow());
    }

    @Test
    void noSePuedeConfirmarSinDireccionDeEnvioYSigueSiendoUnCarrito() {
        Compra compra = compraConDetalle();

        assertThrows(ReglaDominioException.class, () -> compra.confirmar(null));
        assertEquals(EstadoDeCompra.PENDIENTE, compra.getEstado());
        assertTrue(compra.getDireccionEnvio().isEmpty());
    }
}
