package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.AsistenteIA;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.MensajeDeChat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarAsistenteTest {

    @Mock
    private PresentacionRepositorio presentaciones;

    @Mock
    private AsistenteIA asistente;

    @InjectMocks
    private ConsultarAsistente consultarAsistente;

    @Test
    void elCatalogoVaEnLasInstruccionesYSeDevuelveLaRespuestaDeLaIA() {
        Presentacion cafe = PresentacionesDePrueba.cafeVerde("pres-1");
        when(presentaciones.buscarDisponibles(any(FiltroDePresentaciones.class), anyInt(), anyInt()))
                .thenReturn(new Pagina<>(List.of(cafe), 0, 30, 1));
        when(asistente.responder(anyString(), anyList(), eq("¿Qué me recomiendas?")))
                .thenReturn("Te recomiendo el Café Verde.");

        String respuesta = consultarAsistente.ejecutar("¿Qué me recomiendas?", List.of());

        assertEquals("Te recomiendo el Café Verde.", respuesta);
        ArgumentCaptor<String> instrucciones = ArgumentCaptor.forClass(String.class);
        verify(asistente).responder(instrucciones.capture(), anyList(), eq("¿Qué me recomiendas?"));
        assertTrue(instrucciones.getValue().contains("Café Verde"));
        assertTrue(instrucciones.getValue().contains("28000"));
    }

    @Test
    void unMensajeEnBlancoLanzaReglaDeDominioYNoLlamaALaIA() {
        assertThrows(ReglaDominioException.class, () -> consultarAsistente.ejecutar("   ", List.of()));

        verifyNoInteractions(asistente);
    }

    @Test
    void aLaIASoloLlegaranLosUltimosSeisMensajesDelHistorial() {
        when(presentaciones.buscarDisponibles(any(FiltroDePresentaciones.class), anyInt(), anyInt()))
                .thenReturn(new Pagina<>(List.of(), 0, 30, 0));
        List<MensajeDeChat> historial = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            historial.add(new MensajeDeChat("usuario", "mensaje " + i));
        }

        consultarAsistente.ejecutar("hola", historial);

        ArgumentCaptor<List<MensajeDeChat>> enviado = ArgumentCaptor.forClass(List.class);
        verify(asistente).responder(anyString(), enviado.capture(), eq("hola"));
        assertEquals(6, enviado.getValue().size());
        assertEquals("mensaje 5", enviado.getValue().get(0).texto());
        assertEquals("mensaje 10", enviado.getValue().get(5).texto());
    }

    @Test
    void unHistorialNuloSeTrataComoVacio() {
        when(presentaciones.buscarDisponibles(any(FiltroDePresentaciones.class), anyInt(), anyInt()))
                .thenReturn(new Pagina<>(List.of(), 0, 30, 0));

        consultarAsistente.ejecutar("hola", null);

        verify(asistente).responder(anyString(), eq(List.of()), eq("hola"));
    }
}
