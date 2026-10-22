package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReembolso;
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
import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.cafeVerde;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Usa el ServicioDeReembolso real: así la prueba cubre las reglas y no solo la delegación. */
@ExtendWith(MockitoExtension.class)
class SolicitarReembolsoTest {

    @Mock
    private CompraRepositorio compraRepositorio;

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    @Mock
    private SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;

    private SolicitarReembolso solicitarReembolso;

    @BeforeEach
    void preparar() {
        solicitarReembolso = new SolicitarReembolso(compraRepositorio, presentacionRepositorio,
                solicitudReembolsoRepositorio, new ServicioDeReembolso(solicitudReembolsoRepositorio));
    }

    private void conCompraEntregadaHace(int horas) {
        Compra compra = entregada("compra-1", LocalDateTime.now().minusHours(horas));
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(cafeVerde("pres-1")));
    }

    @Test
    void dentroDeLas48HorasCreaLaSolicitudYLaGuarda() {
        conCompraEntregadaHace(5);

        SolicitudReembolso solicitud = solicitarReembolso.ejecutar("reembolso-1", "compra-1", "comprador-1",
                "pres-1", "Llego humedo");

        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
        assertEquals("vendedor-1", solicitud.getVendedorId());
        verify(solicitudReembolsoRepositorio).guardar(solicitud);
    }

    @Test
    void fueraDeLas48HorasNoSeGuardaNada() {
        conCompraEntregadaHace(49);

        assertThrows(ReglaDominioException.class, () -> solicitarReembolso.ejecutar("reembolso-1", "compra-1",
                "comprador-1", "pres-1", "Llego humedo"));

        verify(solicitudReembolsoRepositorio, never()).guardar(any());
    }

    @Test
    void siYaHayUnaSolicitudVigenteNoSeGuardaOtra() {
        conCompraEntregadaHace(5);
        when(solicitudReembolsoRepositorio.existeSolicitudVigente("compra-1", "pres-1")).thenReturn(true);

        assertThrows(ReglaDominioException.class, () -> solicitarReembolso.ejecutar("reembolso-2", "compra-1",
                "comprador-1", "pres-1", "Llego humedo"));

        verify(solicitudReembolsoRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaCompraNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> solicitarReembolso.ejecutar("reembolso-1", "no-existe",
                "comprador-1", "pres-1", "Llego humedo"));

        verify(solicitudReembolsoRepositorio, never()).guardar(any());
    }
}
