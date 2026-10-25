package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioDeTransformacionTest {

    @Mock
    private TransformacionRepositorio transformacionRepositorio;

    @Mock
    private LoteRepositorio loteRepositorio;

    private ServicioDeTransformacion servicio;
    private Lote lote;

    @BeforeEach
    void setUp() {
        servicio = new ServicioDeTransformacion(transformacionRepositorio, loteRepositorio);

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
    void transformaConMermaCorrectaYCalculaResultante() {
        when(loteRepositorio.buscarPorCodigo(new CodigoDeLote("LOT-2026-001"))).thenReturn(Optional.of(lote));

        // Merma válida del 18% (0.18) y entrada de 50 kg -> Resultante debe ser 41 kg
        Transformacion transformacion = servicio.transformar(
                "TRANS-1",
                "LOT-2026-001",
                new Cantidad(50.0, "kg"),
                new PorcentajeDeMerma(0.18),
                "tostador-1",
                LocalDate.now(),
                PerfilTueste.MEDIO
        );

        assertNotNull(transformacion);
        assertEquals(41.0, transformacion.getCantidadResultante().valor(), 0.0001);
        assertEquals(50.0, lote.getCantidadDisponible().valor(), 0.0001); // 100 - 50 = 50 kg

        verify(loteRepositorio, times(1)).guardar(lote);
        verify(transformacionRepositorio, times(1)).guardar(transformacion);
    }

    @Test
    void rechazaTransformacionCuandoCantidadEsMayorQueLaDisponibleEnLoteYNoGuarda() {
        when(loteRepositorio.buscarPorCodigo(new CodigoDeLote("LOT-2026-001"))).thenReturn(Optional.of(lote));

        // Intentar transformar 150 kg cuando el Lote solo tiene 100 kg
        assertThrows(ReglaDominioException.class, () ->
                servicio.transformar(
                        "TRANS-2",
                        "LOT-2026-001",
                        new Cantidad(150.0, "kg"),
                        new PorcentajeDeMerma(0.18),
                        "tostador-1",
                        LocalDate.now(),
                        PerfilTueste.MEDIO
                )
        );

        assertEquals(100.0, lote.getCantidadDisponible().valor(), 0.0001); // El lote permanece intacto
        verify(transformacionRepositorio, never()).guardar(any());
    }

    @Test
    void rechazaMermaFueraDelRangoDe12a22PorCientoYNoAccedeAlLote() {
        // Merma del 10% (< 12%)
        assertThrows(ReglaDominioException.class, () ->
                servicio.transformar(
                        "TRANS-3",
                        "LOT-2026-001",
                        new Cantidad(50.0, "kg"),
                        new PorcentajeDeMerma(0.10),
                        "tostador-1",
                        LocalDate.now(),
                        PerfilTueste.MEDIO
                )
        );

        // Verify que no se llegó a buscar ni modificar el Lote
        verify(loteRepositorio, never()).buscarPorCodigo(any());
        verify(loteRepositorio, never()).guardar(any());
        verify(transformacionRepositorio, never()).guardar(any());
    }
}