package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalificacionTest {

    @Test
    void aceptaValoresDeUnoACinco() {
        for (int valor = 1; valor <= 5; valor++) {
            assertEquals(valor, new Calificacion(valor).valor());
        }
    }

    @Test
    void noAceptaCeroNiNegativos() {
        assertThrows(ReglaDominioException.class, () -> new Calificacion(0));
        assertThrows(ReglaDominioException.class, () -> new Calificacion(-3));
    }

    @Test
    void noAceptaMasDeCinco() {
        assertThrows(ReglaDominioException.class, () -> new Calificacion(6));
    }

    @Test
    void dosCalificacionesConElMismoValorSonIguales() {
        assertEquals(new Calificacion(4), new Calificacion(4));
    }
}
