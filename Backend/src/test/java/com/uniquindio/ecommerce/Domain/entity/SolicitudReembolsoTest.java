package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudReembolsoTest {

    private static final LocalDateTime SOLICITADA = LocalDateTime.of(2026, 10, 21, 10, 0);
    private static final LocalDateTime RESUELTA = LocalDateTime.of(2026, 10, 21, 16, 30);

    private SolicitudReembolso solicitud() {
        return SolicitudReembolso.solicitar("reembolso-1", "compra-1", "pres-1", "vendedor-1",
                "El cafe llego humedo", SOLICITADA);
    }

    @Test
    void solicitarGuardaLaCompraLaPresentacionYElVendedorYNaceSolicitada() {
        SolicitudReembolso solicitud = solicitud();

        assertEquals("compra-1", solicitud.getCompraId());
        assertEquals("pres-1", solicitud.getPresentacionId());
        assertEquals("vendedor-1", solicitud.getVendedorId());
        assertEquals(SOLICITADA, solicitud.getFechaSolicitud());
        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
        assertTrue(solicitud.getFechaResolucion().isEmpty());
    }

    @Test
    void noSePuedeSolicitarSinIndicarLaPresentacionNiElVendedor() {
        assertThrows(ReglaDominioException.class, () -> SolicitudReembolso.solicitar("reembolso-1", "compra-1",
                " ", "vendedor-1", "El cafe llego humedo", SOLICITADA));
        assertThrows(ReglaDominioException.class, () -> SolicitudReembolso.solicitar("reembolso-1", "compra-1",
                "pres-1", null, "El cafe llego humedo", SOLICITADA));
    }

    @Test
    void noSePuedeSolicitarSinCompraMotivoNiFecha() {
        assertThrows(ReglaDominioException.class, () -> SolicitudReembolso.solicitar("reembolso-1", "",
                "pres-1", "vendedor-1", "El cafe llego humedo", SOLICITADA));
        assertThrows(ReglaDominioException.class, () -> SolicitudReembolso.solicitar("reembolso-1", "compra-1",
                "pres-1", "vendedor-1", "  ", SOLICITADA));
        assertThrows(ReglaDominioException.class, () -> SolicitudReembolso.solicitar("reembolso-1", "compra-1",
                "pres-1", "vendedor-1", "El cafe llego humedo", null));
    }

    @Test
    void aprobarRegistraLaFechaYHoraDeLaResolucion() {
        SolicitudReembolso solicitud = solicitud();

        solicitud.aprobar(RESUELTA);

        assertEquals(EstadoDeReembolso.APROBADO, solicitud.getEstado());
        assertEquals(RESUELTA, solicitud.getFechaResolucion().orElseThrow());
    }

    @Test
    void unaSolicitudSoloSeResuelveUnaVezYConservaLaPrimeraResolucion() {
        SolicitudReembolso solicitud = solicitud();
        solicitud.rechazar(RESUELTA);

        assertThrows(ReglaDominioException.class, () -> solicitud.aprobar(RESUELTA.plusHours(1)));

        assertEquals(EstadoDeReembolso.RECHAZADO, solicitud.getEstado());
        assertEquals(RESUELTA, solicitud.getFechaResolucion().orElseThrow());
    }

    @Test
    void resolverSinFechaFallaYLaSolicitudSigueSolicitada() {
        SolicitudReembolso solicitud = solicitud();

        assertThrows(ReglaDominioException.class, () -> solicitud.rechazar(null));

        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
    }

    @Test
    void reconstruirDevuelveLaSolicitudTalComoSeGuardo() {
        SolicitudReembolso solicitud = SolicitudReembolso.reconstruir("reembolso-1", "compra-1", "pres-1",
                "vendedor-1", "El cafe llego humedo", SOLICITADA, EstadoDeReembolso.APROBADO, RESUELTA);

        assertEquals(EstadoDeReembolso.APROBADO, solicitud.getEstado());
        assertEquals(RESUELTA, solicitud.getFechaResolucion().orElseThrow());
        assertEquals("pres-1", solicitud.getPresentacionId());
    }
}
