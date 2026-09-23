package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CantidadInvarianteTest {

    @Test
    @DisplayName("Invariante Cantidad - Restar mas de lo disponible lanza ReglaDominioException")
    void restar_deberiaLanzarExcepcion_cuandoRestaSuperaDisponible() {

        Cantidad disponible = new Cantidad(5.0, "kg");
        Cantidad aRestar = new Cantidad(10.0, "kg");

        assertThrows(
                ReglaDominioException.class,
                () -> disponible.restar(aRestar)
        );
    }

    @Test
    @DisplayName("Invariante Cantidad - El estado original permanece inalterado tras un rechazo")
    void restar_estadoNoCambia_cuandoOperacionEsRechazada() {

        Cantidad original = new Cantidad(5.0, "kg");
        Cantidad invalida = new Cantidad(20.0, "kg");


        try {
            original.restar(invalida);
        } catch (ReglaDominioException e) {
        }


        assertEquals(5.0, original.valor(), "El valor original no debió cambiar tras fallar la resta");
    }
}