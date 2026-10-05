package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class CosechaTest {

    private final int anioActual = Year.now().getValue();

    @Test
    void dosCosechasConElMismoAnioYTemporadaSonIguales() {
        Cosecha c1 = new Cosecha(anioActual, TemporadaDeCosecha.TRAVIESA);
        Cosecha c2 = new Cosecha(anioActual, TemporadaDeCosecha.TRAVIESA);

        assertEquals(c1, c2);
    }

    @Test
    void seAceptaLaCosechaDelAnioAnterior() {
        Cosecha cosecha = new Cosecha(anioActual - 1, TemporadaDeCosecha.TRAVIESA);

        assertEquals(anioActual - 1, cosecha.anio());
    }

    @Test
    void noSeAceptaUnaCosechaDeUnAnioFuturo() {
        assertThrows(ReglaDominioException.class, () -> new Cosecha(anioActual + 1, TemporadaDeCosecha.PRINCIPAL));
    }

    @Test
    void noSeAceptaUnaCosechaDeHaceDosAniosOMas() {
        assertThrows(ReglaDominioException.class, () -> new Cosecha(anioActual - 2, TemporadaDeCosecha.PRINCIPAL));
    }

    @Test
    void noSeAceptaUnaCosechaSinTemporada() {
        assertThrows(ReglaDominioException.class, () -> new Cosecha(anioActual, null));
    }
}
