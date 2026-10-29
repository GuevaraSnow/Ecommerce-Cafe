package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoteInvarianteTest {

    private Lote crearLoteConStock(double cantidadKg) {
        return Lote.registrar(
                new CodigoDeLote("LOT-2026-001"),
                new Cantidad(cantidadKg, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.VERDE,
                new Cosecha(2026, TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Castillo")
        );
    }

    @Test
    @DisplayName("Invariante Lote - Descontar una cantidad invalida lanza ReglaDominioException")
    void descontar_deberiaLanzarExcepcion_cuandoCantidadEsInvalida() {
        // ARRANGE: Un Lote real con stock disponible
        Lote lote = crearLoteConStock(9.0);
        Cantidad solicitudInvalida = new Cantidad(15.0, "kg");

        // ACT & ASSERT: Excepción esperada al intentar descontar más del stock disponible
        assertThrows(
                ReglaDominioException.class,
                () -> lote.descontar(solicitudInvalida)
        );
    }

    @Test
    @DisplayName("Invariante Lote - El stock del lote permanece inalterado tras el rechazo de la transformacion")
    void descontar_estadoNoCambia_cuandoFallaLaTransformacion() {
        // ARRANGE: Un Lote real con stock inicial de 10.0 kg
        Lote lote = crearLoteConStock(10.0);
        Cantidad solicitudExcesiva = new Cantidad(50.0, "kg");

        // ACT: Intento de descuento fallido
        try {
            lote.descontar(solicitudExcesiva);
        } catch (ReglaDominioException e) {
            // Captura de la excepción de dominio
        }

        // ASSERT: Verificación de que la cantidad disponible del Lote se mantiene intacta
        assertEquals(10.0, lote.getCantidadDisponible().valor(), "El stock del lote debió mantenerse intacto en 10.0 kg");
    }
}