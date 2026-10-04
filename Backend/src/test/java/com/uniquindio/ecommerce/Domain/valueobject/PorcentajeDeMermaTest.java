package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PorcentajeDeMermaTest {

    @Test
    @DisplayName("Debería crear PorcentajeDeMerma cuando el valor está dentro del rango válido (0.12 - 0.22)")
    void crear_deberiaCrearInstancia_cuandoPorcentajeEsValido() {
        PorcentajeDeMerma merma = new PorcentajeDeMerma(0.18);
        assertEquals(0.18, merma.valor());
    }

    @Test
    @DisplayName("Debería lanzar ReglaDominioException cuando la merma es menor al 12% (0.12)")
    void crear_deberiaLanzarExcepcion_cuandoPorcentajeEsMenorAlMinimo() {
        assertThrows(ReglaDominioException.class, () -> new PorcentajeDeMerma(0.10));
    }

    @Test
    @DisplayName("Debería lanzar ReglaDominioException cuando la merma es mayor al 22% (0.22)")
    void crear_deberiaLanzarExcepcion_cuandoPorcentajeEsMayorAlMaximo() {
        assertThrows(ReglaDominioException.class, () -> new PorcentajeDeMerma(0.25));
    }
}