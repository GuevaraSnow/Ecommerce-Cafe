package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoteInvarianteTest {

    @Test
    @DisplayName("Invariante Lote - Descontar una cantidad invalida lanza ReglaDominioException")
    void descontar_deberiaLanzarExcepcion_cuandoCantidadEsInvalida() {
        // ARRANGE
        Cantidad stockLote = new Cantidad(10.0, "kg");
        Cantidad solicitudInvalida = new Cantidad(15.0, "kg");

        // ACT & ASSERT: Excepción esperada al intentar operarla
        assertThrows(
                ReglaDominioException.class,
                () -> stockLote.restar(solicitudInvalida)
        );
    }

    @Test
    @DisplayName("Invariante Lote - El stock del lote permanece inalterado tras el rechazo de la transformacion")
    void descontar_estadoNoCambia_cuandoFallaLaTransformacion() {
        // ARRANGE
        Cantidad stockInicial = new Cantidad(10.0, "kg");
        Cantidad solicitudExcesiva = new Cantidad(50.0, "kg");

        // ACT
        try {
            stockInicial.restar(solicitudExcesiva);
        } catch (ReglaDominioException e) {
            // Captura de la excepción de dominio
        }

        // ASSERT: Verificación de que el estado inicial no cambió tras el rechazo
        assertEquals(10.0, stockInicial.valor(), "El stock del lote debió mantenerse intacto en 10.0 kg");
    }
}