package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class LoteTest {

    private Lote loteValido(String codigo, double cantidadKg) {
        return Lote.registrar(
                new CodigoDeLote(codigo),
                new Cantidad(cantidadKg, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA),
                new VariedadDeCafe("Caturra"));
    }

    @Test
    void dosLotesConElMismoCodigoSonElMismoAunqueCambienOtrosDatos() {
        Lote original = loteValido("LOT-2024-045", 200);
        Lote otro = loteValido("LOT-2024-045", 50);

        assertEquals(original, otro);
    }

    @Test
    void noSePuedeRegistrarUnLoteDirectamenteEnEstadoTostado() {
        assertThrows(ReglaDominioException.class, () -> Lote.registrar(
                new CodigoDeLote("LOT-2024-046"),
                new Cantidad(100, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Honey,
                EstadoDelCafe.TOSTADO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Castillo")));
    }

    @Test
    void noSePuedeDescontarMasCantidadDeLaDisponibleYElEstadoNoCambia() {
        Lote lote = loteValido("LOT-2024-047", 50);

        assertThrows(ReglaDominioException.class, () -> lote.descontar(new Cantidad(80, "kg")));
        assertEquals(new Cantidad(50, "kg"), lote.getCantidadDisponible());
    }

    @Test
    void noSePuedeDescontarCantidadDeUnLoteEliminadoYElEstadoNoCambia() {
        Lote lote = loteValido("LOT-2024-048", 100);
        lote.eliminarLogicamente();

        assertThrows(ReglaDominioException.class, () -> lote.descontar(new Cantidad(10, "kg")));
        assertEquals(new Cantidad(100, "kg"), lote.getCantidadDisponible());
    }
}
