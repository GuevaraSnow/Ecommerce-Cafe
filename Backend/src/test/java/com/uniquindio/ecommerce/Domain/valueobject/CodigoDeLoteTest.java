package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodigoDeLoteTest {

    @Test
    void dosCodigosConElMismoValorDebenSerIguales() {
        CodigoDeLote c1 = new CodigoDeLote("LOT-2024-045");
        CodigoDeLote c2 = new CodigoDeLote("LOT-2024-045");

        assertEquals(c1, c2);
    }

    @Test
    void noDebePermitirUnCodigoConFormatoInvalido() {
        assertThrows(ReglaDominioException.class, () -> new CodigoDeLote("Lote-045"));
    }
}
