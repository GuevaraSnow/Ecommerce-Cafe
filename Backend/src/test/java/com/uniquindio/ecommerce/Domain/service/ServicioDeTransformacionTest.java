package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

/** El servicio aplica la regla (merma y descuento del Lote) sobre un Lote ya cargado; no guarda ni busca nada. */
class ServicioDeTransformacionTest {

    private ServicioDeTransformacion servicio;
    private Lote lote;

    @BeforeEach
    void setUp() {
        servicio = new ServicioDeTransformacion();

        lote = Lote.registrar(
                new CodigoDeLote("LOT-2026-001"),
                new Cantidad(100.0, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Castillo")
        );
    }

    @Test
    void transformaConMermaCorrectaCalculaElResultanteYDescuentaDelLote() {
        // Merma válida del 18% (0.18) y entrada de 50 kg -> Resultante debe ser 41 kg
        Transformacion transformacion = servicio.transformar(
                "TRANS-1",
                lote,
                new Cantidad(50.0, "kg"),
                new PorcentajeDeMerma(0.18),
                "tostador-1",
                LocalDate.now(),
                PerfilTueste.MEDIO
        );

        assertEquals("LOT-2026-001", transformacion.getLoteCodigo());
        assertEquals(41.0, transformacion.getCantidadResultante().valor(), 0.0001);
        assertEquals(50.0, lote.getCantidadDisponible().valor(), 0.0001); // 100 - 50 = 50 kg
    }

    @Test
    void rechazaUnaCantidadMayorQueLaDisponibleDelLoteYElLoteQuedaIntacto() {
        assertThrows(ReglaDominioException.class, () ->
                servicio.transformar(
                        "TRANS-2",
                        lote,
                        new Cantidad(150.0, "kg"),
                        new PorcentajeDeMerma(0.18),
                        "tostador-1",
                        LocalDate.now(),
                        PerfilTueste.MEDIO
                )
        );

        assertEquals(100.0, lote.getCantidadDisponible().valor(), 0.0001); // El lote permanece intacto
    }

    @Test
    void rechazaUnLoteDadoDeBajaYNoCambiaSuCantidad() {
        lote.eliminarLogicamente();

        assertThrows(ReglaDominioException.class, () ->
                servicio.transformar(
                        "TRANS-3",
                        lote,
                        new Cantidad(50.0, "kg"),
                        new PorcentajeDeMerma(0.18),
                        "tostador-1",
                        LocalDate.now(),
                        PerfilTueste.MEDIO
                )
        );

        assertEquals(100.0, lote.getCantidadDisponible().valor(), 0.0001);
    }

    @Test
    void siFaltaUnDatoDeLaTransformacionElLoteNoSeDescuenta() {
        assertThrows(ReglaDominioException.class, () ->
                servicio.transformar(
                        "TRANS-4",
                        lote,
                        new Cantidad(50.0, "kg"),
                        new PorcentajeDeMerma(0.18),
                        " ",
                        LocalDate.now(),
                        PerfilTueste.MEDIO
                )
        );

        assertEquals(100.0, lote.getCantidadDisponible().valor(), 0.0001);
    }

    @Test
    void laMermaFueraDelRangoDe12a22PorCientoSeRechazaAlCrearla() {
        assertThrows(ReglaDominioException.class, () -> new PorcentajeDeMerma(0.10));
        assertThrows(ReglaDominioException.class, () -> new PorcentajeDeMerma(0.25));
    }
}
