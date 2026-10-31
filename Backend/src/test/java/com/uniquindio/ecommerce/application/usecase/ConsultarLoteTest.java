package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Las consultas solo delegan en el repositorio y nunca guardan.
 */
@ExtendWith(MockitoExtension.class)
class ConsultarLoteTest {

    @Mock
    private LoteRepositorio loteRepositorio;

    @InjectMocks
    private ConsultarLote consultarLote;

    @Test
    void consultarDevuelveElLote() {
        CodigoDeLote codigoDeLote = CodigoDeLote.crear(Year.now().getValue(), 1);
        Lote lote = Lote.registrar(
                codigoDeLote, new Cantidad(100, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));

        when(loteRepositorio.buscarNoEliminadoPorCodigo(codigoDeLote)).thenReturn(Optional.of(lote));

        assertSame(lote, consultarLote.ejecutar(codigoDeLote));
        verify(loteRepositorio, never()).guardar(any());
    }

    @Test
    void consultarUnLoteInexistenteLanzaNoEncontrado() {
        CodigoDeLote codigoInexistente = CodigoDeLote.crear(Year.now().getValue(), 999);
        when(loteRepositorio.buscarNoEliminadoPorCodigo(codigoInexistente)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> consultarLote.ejecutar(codigoInexistente));
        verify(loteRepositorio, never()).guardar(any());
    }

    @Test
    void consultarUnLoteDadoDeBajaLanzaNoEncontradoSinBuscarloConLaVariantePlana() {
        CodigoDeLote codigoDeBaja = CodigoDeLote.crear(Year.now().getValue(), 7);
        when(loteRepositorio.buscarNoEliminadoPorCodigo(codigoDeBaja)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> consultarLote.ejecutar(codigoDeBaja));

        verify(loteRepositorio, never()).buscarPorCodigo(any());
    }
}
