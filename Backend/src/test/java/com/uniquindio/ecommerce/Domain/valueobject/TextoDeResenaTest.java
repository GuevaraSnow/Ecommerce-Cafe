package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextoDeResenaTest {

    @Test
    void guardaElTextoSinEspaciosSobrantesEnLosExtremos() {
        assertEquals("Notas a panela y buen cuerpo", new TextoDeResena("  Notas a panela y buen cuerpo  ").valor());
    }

    @Test
    void noPuedeEstarVacioNiSerSoloEspacios() {
        assertThrows(ReglaDominioException.class, () -> new TextoDeResena(null));
        assertThrows(ReglaDominioException.class, () -> new TextoDeResena(""));
        assertThrows(ReglaDominioException.class, () -> new TextoDeResena("    "));
    }

    @Test
    void aceptaExactamenteQuinientosCaracteres() {
        assertEquals(500, new TextoDeResena("a".repeat(500)).valor().length());
    }

    @Test
    void noAceptaMasDeQuinientosCaracteres() {
        assertThrows(ReglaDominioException.class, () -> new TextoDeResena("a".repeat(501)));
    }

    @Test
    void losEspaciosDeLosExtremosNoCuentanParaElLimite() {
        assertEquals(500, new TextoDeResena("  " + "a".repeat(500) + "  ").valor().length());
    }
}
