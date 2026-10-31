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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** La baja solo orquesta: busca el Lote, lo marca como eliminado y lo guarda. */
@ExtendWith(MockitoExtension.class)
class EliminarLoteTest {

    @Mock
    private LoteRepositorio loteRepositorio;

    @InjectMocks
    private EliminarLote eliminarLote;

    private Lote loteDePrueba(CodigoDeLote codigo) {
        return Lote.registrar(
                codigo, new Cantidad(100, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));
    }

    @Test
    void daDeBajaLogicaElLoteYLoGuarda() {
        CodigoDeLote codigo = CodigoDeLote.crear(Year.now().getValue(), 1);
        Lote lote = loteDePrueba(codigo);
        when(loteRepositorio.buscarPorCodigo(codigo)).thenReturn(Optional.of(lote));

        Lote eliminado = eliminarLote.ejecutar(codigo);

        assertTrue(eliminado.isEliminadoLogicamente());
        verify(loteRepositorio).guardar(lote);
    }

    @Test
    void daDeBajaUnLoteInexistenteLanzaNoEncontradoYNoGuarda() {
        CodigoDeLote codigo = CodigoDeLote.crear(Year.now().getValue(), 999);
        when(loteRepositorio.buscarPorCodigo(codigo)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> eliminarLote.ejecutar(codigo));

        verify(loteRepositorio, never()).guardar(any());
    }
}
