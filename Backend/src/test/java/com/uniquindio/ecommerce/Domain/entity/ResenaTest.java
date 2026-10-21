package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.RespuestaAResena;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResenaTest {

    private static final LocalDateTime ESCRITA = LocalDateTime.of(2026, 10, 20, 18, 0);
    private static final LocalDateTime RESPONDIDA = LocalDateTime.of(2026, 10, 21, 9, 30);
    private static final TextoDeResena COMENTARIO = new TextoDeResena("Notas a panela y buen cuerpo");
    private static final TextoDeResena GRACIAS = new TextoDeResena("Gracias por tu compra");

    private Resena resena() {
        return Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1",
                new Calificacion(5), COMENTARIO, ESCRITA);
    }

    @Test
    void dejarUnaResenaGuardaSusDatosYNaceSinRespuesta() {
        Resena resena = resena();

        assertEquals("comprador-1", resena.getCompradorId());
        assertEquals("pres-1", resena.getPresentacionId());
        assertEquals("vendedor-1", resena.getVendedorId());
        assertEquals(new Calificacion(5), resena.getCalificacion());
        assertEquals(COMENTARIO, resena.getTexto());
        assertEquals(ESCRITA, resena.getFecha());
        assertTrue(resena.getRespuesta().isEmpty());
    }

    @Test
    void noSePuedeDejarUnaResenaSinComprador() {
        assertThrows(ReglaDominioException.class, () -> Resena.dejar("resena-1", " ", "pres-1", "vendedor-1",
                new Calificacion(5), COMENTARIO, ESCRITA));
    }

    @Test
    void noSePuedeDejarUnaResenaSinPresentacionNiVendedor() {
        assertThrows(ReglaDominioException.class, () -> Resena.dejar("resena-1", "comprador-1", null, "vendedor-1",
                new Calificacion(5), COMENTARIO, ESCRITA));
        assertThrows(ReglaDominioException.class, () -> Resena.dejar("resena-1", "comprador-1", "pres-1", "",
                new Calificacion(5), COMENTARIO, ESCRITA));
    }

    @Test
    void noSePuedeDejarUnaResenaSinCalificacionTextoNiFecha() {
        assertThrows(ReglaDominioException.class, () -> Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1",
                null, COMENTARIO, ESCRITA));
        assertThrows(ReglaDominioException.class, () -> Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1",
                new Calificacion(5), null, ESCRITA));
        assertThrows(ReglaDominioException.class, () -> Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1",
                new Calificacion(5), COMENTARIO, null));
    }

    @Test
    void elVendedorDuenoDeLaPresentacionPuedeResponder() {
        Resena resena = resena();

        resena.responder("vendedor-1", GRACIAS, RESPONDIDA);

        assertEquals(new RespuestaAResena(GRACIAS, RESPONDIDA), resena.getRespuesta().orElseThrow());
    }

    @Test
    void otroVendedorNoPuedeResponderYLaResenaSigueSinRespuesta() {
        Resena resena = resena();

        assertThrows(ReglaDominioException.class, () -> resena.responder("vendedor-2", GRACIAS, RESPONDIDA));

        assertTrue(resena.getRespuesta().isEmpty());
    }

    @Test
    void unaResenaSoloSeRespondeUnaVezYConservaLaPrimeraRespuesta() {
        Resena resena = resena();
        resena.responder("vendedor-1", GRACIAS, RESPONDIDA);

        assertThrows(ReglaDominioException.class,
                () -> resena.responder("vendedor-1", new TextoDeResena("Otra respuesta"), RESPONDIDA.plusHours(1)));

        assertEquals(GRACIAS, resena.getRespuesta().orElseThrow().texto());
    }

    @Test
    void responderSinFechaFallaYLaResenaSigueSinRespuesta() {
        Resena resena = resena();

        assertThrows(ReglaDominioException.class, () -> resena.responder("vendedor-1", GRACIAS, null));

        assertTrue(resena.getRespuesta().isEmpty());
    }

    @Test
    void reconstruirDevuelveLaResenaTalComoSeGuardoConSuRespuesta() {
        RespuestaAResena respuesta = new RespuestaAResena(GRACIAS, RESPONDIDA);

        Resena resena = Resena.reconstruir("resena-1", "comprador-1", "pres-1", "vendedor-1",
                new Calificacion(3), COMENTARIO, ESCRITA, respuesta);

        assertEquals("resena-1", resena.getId());
        assertEquals(new Calificacion(3), resena.getCalificacion());
        assertEquals(respuesta, resena.getRespuesta().orElseThrow());
    }

    @Test
    void dosResenasConElMismoTextoYCalificacionSonDistintasSiTienenDistintoId() {
        Resena primera = resena();
        Resena segunda = Resena.dejar("resena-2", "comprador-1", "pres-1", "vendedor-1",
                new Calificacion(5), COMENTARIO, ESCRITA);

        assertNotEquals(primera, segunda);
    }
}
