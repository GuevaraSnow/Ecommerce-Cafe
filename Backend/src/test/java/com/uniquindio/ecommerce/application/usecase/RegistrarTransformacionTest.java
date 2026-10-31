package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeTransformacion;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El caso de uso carga el Lote (no encontrado si no existe), usa el servicio real y guarda ambos. */
@ExtendWith(MockitoExtension.class)
class RegistrarTransformacionTest {

    private static final CodigoDeLote CODIGO = new CodigoDeLote("LOT-2026-001");

    @Mock
    private LoteRepositorio loteRepositorio;
    @Mock
    private TransformacionRepositorio transformacionRepositorio;

    private RegistrarTransformacion registrarTransformacion;
    private Lote lote;

    @BeforeEach
    void setUp() {
        registrarTransformacion = new RegistrarTransformacion(
                new ServicioDeTransformacion(), loteRepositorio, transformacionRepositorio);
        lote = Lote.registrar(CODIGO, new Cantidad(100.0, "kg"), "finca-1", ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO, new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Castillo"));
    }

    private Transformacion ejecutar(Cantidad entrada) {
        return registrarTransformacion.ejecutar("trans-1", CODIGO, entrada, new PorcentajeDeMerma(0.18),
                "tostador-1", LocalDate.now(), PerfilTueste.MEDIO);
    }

    @Test
    void descuentaElLoteYGuardaElLoteYLaTransformacion() {
        when(loteRepositorio.buscarPorCodigo(CODIGO)).thenReturn(Optional.of(lote));

        Transformacion transformacion = ejecutar(new Cantidad(50.0, "kg"));

        assertEquals(41.0, transformacion.getCantidadResultante().valor(), 0.0001);
        assertEquals(50.0, lote.getCantidadDisponible().valor(), 0.0001);
        verify(loteRepositorio).guardar(lote);
        verify(transformacionRepositorio).guardar(transformacion);
    }

    @Test
    void siElLoteNoExisteLanzaNoEncontradoYNoGuardaNada() {
        when(loteRepositorio.buscarPorCodigo(CODIGO)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> ejecutar(new Cantidad(50.0, "kg")));

        verify(loteRepositorio, never()).guardar(any());
        verify(transformacionRepositorio, never()).guardar(any());
    }

    @Test
    void siLaCantidadSuperaLaDelLoteSeRechazaYNoSeGuardaNada() {
        when(loteRepositorio.buscarPorCodigo(CODIGO)).thenReturn(Optional.of(lote));

        assertThrows(ReglaDominioException.class, () -> ejecutar(new Cantidad(150.0, "kg")));

        assertEquals(100.0, lote.getCantidadDisponible().valor(), 0.0001);
        verify(loteRepositorio, never()).guardar(any());
        verify(transformacionRepositorio, never()).guardar(any());
    }
}
