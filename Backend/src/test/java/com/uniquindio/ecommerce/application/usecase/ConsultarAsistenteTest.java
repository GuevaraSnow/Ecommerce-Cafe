package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.AsistenteIA;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Descuento;
import com.uniquindio.ecommerce.Domain.valueobject.MensajeDeChat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void unProductoConDescuentoVigenteLlevaSuPrecioNormalElPorcentajeYLaFechaFinal() {
        Presentacion conDescuento = PresentacionesDePrueba.cafeVerde("pres-d");
        LocalDate fin = LocalDate.now().plusDays(5);
        conDescuento.programarDescuento(new Descuento(20, LocalDate.now().minusDays(1), fin));
        when(presentaciones.buscarDisponibles(any(FiltroDePresentaciones.class), anyInt(), anyInt()))
                .thenReturn(new Pagina<>(List.of(conDescuento), 0, 30, 1));

        consultarAsistente.ejecutar("¿Hay ofertas?", List.of());

        ArgumentCaptor<String> instrucciones = ArgumentCaptor.forClass(String.class);
        verify(asistente).responder(instrucciones.capture(), anyList(), eq("¿Hay ofertas?"));
        assertTrue(instrucciones.getValue().contains("28000 COP, ahora 22400 COP con 20 % de descuento hasta " + fin));
    }

    @Test
    void unProductoSinDescuentoNoMencionaDescuentoEnSuLinea() {
        Presentacion sinDescuento = PresentacionesDePrueba.cafeVerde("pres-s");
        when(presentaciones.buscarDisponibles(any(FiltroDePresentaciones.class), anyInt(), anyInt()))
                .thenReturn(new Pagina<>(List.of(sinDescuento), 0, 30, 1));

        consultarAsistente.ejecutar("hola", List.of());

        ArgumentCaptor<String> instrucciones = ArgumentCaptor.forClass(String.class);
        verify(asistente).responder(instrucciones.capture(), anyList(), eq("hola"));
        assertTrue(instrucciones.getValue().contains("- Café Verde | CAFE_VERDE | 28000 COP | disponible: 200 kg"));
        assertFalse(instrucciones.getValue().contains("ahora"));
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
