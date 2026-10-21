package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalificacionTest {

    @Test
    void aceptaDeUnaACincoEstrellas() {
        for (int estrellas = 1; estrellas <= 5; estrellas++) {
            assertEquals(estrellas, new Calificacion(estrellas).estrellas());
        }
    }

    @Test
    void noAceptaCeroNiNegativos() {
        assertThrows(ReglaDominioException.class, () -> new Calificacion(0));
        assertThrows(ReglaDominioException.class, () -> new Calificacion(-3));
    }

    @Test
    void noAceptaMasDeCincoEstrellas() {
        assertThrows(ReglaDominioException.class, () -> new Calificacion(6));
    }

    @Test
    void dosCalificacionesConLasMismasEstrellasSonIguales() {
        assertEquals(new Calificacion(4), new Calificacion(4));
    }
}
