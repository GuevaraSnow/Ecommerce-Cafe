package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RespuestaAResenaTest {

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 21, 10, 0);

    @Test
    void guardaElTextoYLaFechaDeLaRespuesta() {
        RespuestaAResena respuesta = new RespuestaAResena(new TextoDeResena("Gracias por tu compra"), FECHA);

        assertEquals("Gracias por tu compra", respuesta.texto().valor());
        assertEquals(FECHA, respuesta.fecha());
    }

    @Test
    void noSePuedeResponderSinTexto() {
        assertThrows(ReglaDominioException.class, () -> new RespuestaAResena(null, FECHA));
    }

    @Test
    void noSePuedeResponderSinFecha() {
        assertThrows(ReglaDominioException.class,
                () -> new RespuestaAResena(new TextoDeResena("Gracias por tu compra"), null));
    }
}
