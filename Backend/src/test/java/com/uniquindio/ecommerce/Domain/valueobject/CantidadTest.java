package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CantidadTest {

    @Test
    void dosCantidadesConElMismoValorYUnidadDebenSerIguales() {
        Cantidad c1 = new Cantidad(200, "kg");
        Cantidad c2 = new Cantidad(200, "kg");

        assertEquals(c1, c2);
    }

    @Test
    void noDebePermitirCantidadConUnidadInvalida() {
        assertThrows(ReglaDominioException.class, () -> new Cantidad(200, "toneladas"));
    }

    @Test
    void restarDebeLanzarExcepcionSiElResultadoQuedaNegativo() {
        Cantidad disponible = new Cantidad(50, "kg");

        assertThrows(ReglaDominioException.class, () -> disponible.restar(new Cantidad(80, "kg")));
    }
}
