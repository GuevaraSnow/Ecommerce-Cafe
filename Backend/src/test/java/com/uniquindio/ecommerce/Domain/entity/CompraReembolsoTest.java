package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Cuándo una Compra permite pedir el reembolso de una de sus Presentaciones. */
class CompraReembolsoTest {

    private static final LocalDateTime ENTREGA = LocalDateTime.of(2026, 10, 20, 15, 0);

    private Compra compra;

    @BeforeEach
    void preparar() {
        compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(2, "kg"));
        compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        compra.enviar();
    }

    @Test
    void dentroDeLas48HorasDeLaEntregaSePuedePedirElReembolso() {
        compra.entregar(ENTREGA);

        assertDoesNotThrow(() -> compra.verificarQueSePuedeReembolsar("comprador-1", "pres-1", ENTREGA.plusHours(5)));
    }

    @Test
    void justoAlCumplirseLas48HorasTodaviaSePuede() {
        compra.entregar(ENTREGA);

        assertDoesNotThrow(() -> compra.verificarQueSePuedeReembolsar("comprador-1", "pres-1", ENTREGA.plusHours(48)));
    }

    @Test
    void pasadasLas48HorasYaNoSePuede() {
        compra.entregar(ENTREGA);

        assertThrows(ReglaDominioException.class,
                () -> compra.verificarQueSePuedeReembolsar("comprador-1", "pres-1", ENTREGA.plusHours(48).plusMinutes(1)));
    }

    @Test
    void unaCompraQueAunNoSeEntregaNoSePuedeReembolsar() {
        assertThrows(ReglaDominioException.class,
                () -> compra.verificarQueSePuedeReembolsar("comprador-1", "pres-1", ENTREGA));
    }

    @Test
    void soloQuienHizoLaCompraPuedePedirElReembolso() {
        compra.entregar(ENTREGA);

        assertThrows(ReglaDominioException.class,
                () -> compra.verificarQueSePuedeReembolsar("comprador-2", "pres-1", ENTREGA.plusHours(1)));
    }

    @Test
    void noSePuedePedirElReembolsoDeUnaPresentacionQueNoEstaEnLaCompra() {
        compra.entregar(ENTREGA);

        assertThrows(ReglaDominioException.class,
                () -> compra.verificarQueSePuedeReembolsar("comprador-1", "pres-9", ENTREGA.plusHours(1)));
    }

    @Test
    void verificarNoCambiaElEstadoDeLaCompra() {
        compra.entregar(ENTREGA);

        compra.verificarQueSePuedeReembolsar("comprador-1", "pres-1", ENTREGA.plusHours(1));

        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
    }

    @Test
    void unaCompraEntregadaSePuedeMarcarComoReembolsada() {
        compra.entregar(ENTREGA);

        compra.reembolsar();

        assertEquals(EstadoDeCompra.REEMBOLSADA, compra.getEstado());
    }

    @Test
    void unaCompraQueNoEstaEntregadaNoSePuedeMarcarComoReembolsadaYNoCambia() {
        assertThrows(ReglaDominioException.class, compra::reembolsar);

        assertEquals(EstadoDeCompra.ENVIADA, compra.getEstado());
    }
}
