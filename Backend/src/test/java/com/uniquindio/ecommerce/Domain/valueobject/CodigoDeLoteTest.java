package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void crearCodigoDeLote() {
        assertEquals(new CodigoDeLote("LOT-2026-007"), CodigoDeLote.crear(2026,7));
    }

    @Test
    void noPermiteMasDe999(){
        assertThrows(ReglaDominioException.class, () -> CodigoDeLote.crear(2026,1000));
    }

    @Test
    void noPermite0(){
        assertThrows(ReglaDominioException.class, () -> CodigoDeLote.crear(2026,0));
    }

    @Test
    void noPermiteAnioDeMenosDe4Cifras(){
        assertThrows(ReglaDominioException.class, () -> CodigoDeLote.crear(226,40));
    }

    @Test
    void limitesDeCodigo(){
        assertDoesNotThrow(() -> CodigoDeLote.crear(2026, 1));
        assertDoesNotThrow(() -> CodigoDeLote.crear(2026, 999));
    }
}
