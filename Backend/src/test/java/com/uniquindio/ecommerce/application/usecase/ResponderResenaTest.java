package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
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
class ResponderResenaTest {

    private static final TextoDeResena GRACIAS = new TextoDeResena("Gracias por tu compra");

    @Mock
    private ResenaRepositorio resenaRepositorio;

    @InjectMocks
    private ResponderResena responderResena;

    private Resena resenaDe(String vendedorId) {
        return Resena.dejar("resena-1", "comprador-1", "pres-1", vendedorId, new Calificacion(4),
                new TextoDeResena("Buen cafe"), LocalDateTime.of(2026, 10, 20, 18, 0));
    }

    @Test
    void elVendedorDuenoRespondeYSeGuardaLaResena() {
        Resena resena = resenaDe("vendedor-1");
        when(resenaRepositorio.buscarPorId("resena-1")).thenReturn(Optional.of(resena));

        Resena respondida = responderResena.ejecutar("resena-1", "vendedor-1", GRACIAS);

        assertEquals(GRACIAS, respondida.getRespuesta().orElseThrow().texto());
        verify(resenaRepositorio).guardar(resena);
    }

    @Test
    void otroVendedorNoPuedeResponderYNoSeGuardaNada() {
        Resena resena = resenaDe("vendedor-1");
        when(resenaRepositorio.buscarPorId("resena-1")).thenReturn(Optional.of(resena));

        assertThrows(ReglaDominioException.class, () -> responderResena.ejecutar("resena-1", "vendedor-2", GRACIAS));

        assertTrue(resena.getRespuesta().isEmpty());
        verify(resenaRepositorio, never()).guardar(any());
    }

    @Test
    void unaResenaYaRespondidaNoSeRespondeOtraVezYNoSeGuardaNada() {
        Resena resena = resenaDe("vendedor-1");
        resena.responder("vendedor-1", GRACIAS, LocalDateTime.of(2026, 10, 21, 9, 0));
        when(resenaRepositorio.buscarPorId("resena-1")).thenReturn(Optional.of(resena));

        assertThrows(ReglaDominioException.class,
                () -> responderResena.ejecutar("resena-1", "vendedor-1", new TextoDeResena("Otra")));

        assertEquals(GRACIAS, resena.getRespuesta().orElseThrow().texto());
        verify(resenaRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaResenaNoExisteYNoGuardaNada() {
        when(resenaRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> responderResena.ejecutar("no-existe", "vendedor-1", GRACIAS));

        verify(resenaRepositorio, never()).guardar(any());
    }
}
