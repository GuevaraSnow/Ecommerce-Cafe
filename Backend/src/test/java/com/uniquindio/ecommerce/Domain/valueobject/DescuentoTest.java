package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DescuentoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 12, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 12, 31);

    @Test
    void dosDescuentosConLosMismosValoresDebenSerIguales() {
        assertEquals(new Descuento(20, INICIO, FIN), new Descuento(20, INICIO, FIN));
    }

    @Test
    void aceptaLosLimitesDelPorcentaje() {
        assertDoesNotThrow(() -> new Descuento(1, INICIO, FIN));
        assertDoesNotThrow(() -> new Descuento(50, INICIO, FIN));
    }

    @Test
    void rechazaUnPorcentajeMenorAlMinimo() {
        assertThrows(ReglaDominioException.class, () -> new Descuento(0, INICIO, FIN));
        assertThrows(ReglaDominioException.class, () -> new Descuento(-10, INICIO, FIN));
    }

    @Test
    void rechazaUnPorcentajeMayorAlMaximo() {
        assertThrows(ReglaDominioException.class, () -> new Descuento(51, INICIO, FIN));
    }

    @Test
    void rechazaFechasFaltantes() {
        assertThrows(ReglaDominioException.class, () -> new Descuento(20, null, FIN));
        assertThrows(ReglaDominioException.class, () -> new Descuento(20, INICIO, null));
    }

    @Test
    void rechazaFechasInvertidas() {
        assertThrows(ReglaDominioException.class, () -> new Descuento(20, FIN, INICIO));
    }

    @Test
    void aceptaUnDescuentoDeUnSoloDia() {
        assertDoesNotThrow(() -> new Descuento(20, INICIO, INICIO));
    }

    @Test
    void rigeDesdeElDiaDeInicioHastaElDiaDeFinAmbosIncluidos() {
        Descuento descuento = new Descuento(20, INICIO, FIN);
        assertTrue(descuento.estaVigenteEn(INICIO));
        assertTrue(descuento.estaVigenteEn(LocalDate.of(2026, 12, 15)));
        assertTrue(descuento.estaVigenteEn(FIN));
    }

    @Test
    void noRigeAntesDelInicioNiDespuesDelFin() {
        Descuento descuento = new Descuento(20, INICIO, FIN);
        assertFalse(descuento.estaVigenteEn(INICIO.minusDays(1)));
        assertFalse(descuento.estaVigenteEn(FIN.plusDays(1)));
    }
}
