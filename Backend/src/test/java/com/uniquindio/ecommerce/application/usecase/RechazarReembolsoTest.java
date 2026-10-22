package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RechazarReembolsoTest {

    @Mock
    private SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;

    @InjectMocks
    private RechazarReembolso rechazarReembolso;

    private SolicitudReembolso solicitud;

    @BeforeEach
    void preparar() {
        solicitud = SolicitudReembolso.solicitar("reembolso-1", "compra-1", "pres-1", "vendedor-1",
                "Llego humedo", LocalDateTime.now().minusHours(1));
    }

    @Test
    void elDuenoRechazaYSeGuardaLaSolicitud() {
        when(solicitudReembolsoRepositorio.buscarPorId("reembolso-1")).thenReturn(Optional.of(solicitud));

        rechazarReembolso.ejecutar("reembolso-1", "vendedor-1");

        assertEquals(EstadoDeReembolso.RECHAZADO, solicitud.getEstado());
        verify(solicitudReembolsoRepositorio).guardar(solicitud);
    }

    @Test
    void otroVendedorNoPuedeRechazarYNoSeGuardaNada() {
        when(solicitudReembolsoRepositorio.buscarPorId("reembolso-1")).thenReturn(Optional.of(solicitud));

        assertThrows(ReglaDominioException.class, () -> rechazarReembolso.ejecutar("reembolso-1", "vendedor-2"));

        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
        verify(solicitudReembolsoRepositorio, never()).guardar(any());
    }

    @Test
    void unaSolicitudYaResueltaNoSeRechazaOtraVezYNoSeGuardaNada() {
        solicitud.aprobar("vendedor-1", LocalDateTime.now());
        when(solicitudReembolsoRepositorio.buscarPorId("reembolso-1")).thenReturn(Optional.of(solicitud));

        assertThrows(ReglaDominioException.class, () -> rechazarReembolso.ejecutar("reembolso-1", "vendedor-1"));

        assertEquals(EstadoDeReembolso.APROBADO, solicitud.getEstado());
        verify(solicitudReembolsoRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaSolicitudNoExisteYNoGuardaNada() {
        when(solicitudReembolsoRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> rechazarReembolso.ejecutar("no-existe", "vendedor-1"));

        verify(solicitudReembolsoRepositorio, never()).guardar(any());
    }
}
