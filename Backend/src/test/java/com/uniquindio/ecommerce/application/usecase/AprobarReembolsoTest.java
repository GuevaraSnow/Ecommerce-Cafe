package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReembolso;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.ComprasDePrueba.entregada;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Usa el ServicioDeReembolso real: así la prueba cubre el paso a REEMBOLSADA y no solo la delegación. */
@ExtendWith(MockitoExtension.class)
class AprobarReembolsoTest {

    private static final LocalDateTime ENTREGA = LocalDateTime.now().minusHours(5);

    @Mock
    private SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;

    @Mock
    private CompraRepositorio compraRepositorio;

    private AprobarReembolso aprobarReembolso;
    private Compra compra;
    private SolicitudReembolso solicitud;

    @BeforeEach
    void preparar() {
        aprobarReembolso = new AprobarReembolso(solicitudReembolsoRepositorio, compraRepositorio,
                new ServicioDeReembolso(solicitudReembolsoRepositorio));
        compra = entregada("compra-1", ENTREGA);
        solicitud = SolicitudReembolso.solicitar("reembolso-1", "compra-1", "pres-1", "vendedor-1",
                "Llego humedo", ENTREGA.plusHours(1));
        when(solicitudReembolsoRepositorio.buscarPorId("reembolso-1")).thenReturn(Optional.of(solicitud));
    }

    @Test
    void elDuenoApruebaYComoEraLaUnicaPresentacionLaCompraQuedaReembolsada() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));

        aprobarReembolso.ejecutar("reembolso-1", "vendedor-1");

        assertEquals(EstadoDeReembolso.APROBADO, solicitud.getEstado());
        assertEquals(EstadoDeCompra.REEMBOLSADA, compra.getEstado());
        verify(solicitudReembolsoRepositorio).guardar(solicitud);
        verify(compraRepositorio).guardar(compra);
    }

    @Test
    void otroVendedorNoPuedeAprobarYNoSeGuardaNada() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));

        assertThrows(ReglaDominioException.class, () -> aprobarReembolso.ejecutar("reembolso-1", "vendedor-2"));

        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
        verify(solicitudReembolsoRepositorio, never()).guardar(any());
        verify(compraRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaCompraNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> aprobarReembolso.ejecutar("reembolso-1", "vendedor-1"));

        verify(solicitudReembolsoRepositorio, never()).guardar(any());
        verify(compraRepositorio, never()).guardar(any());
    }
}
