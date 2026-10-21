package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetirarStockDePresentacionTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private RetirarStockDePresentacion retirarStock;

    @Test
    void retiraLaCantidadDelStockYGuardaLaPresentacion() {
        Presentacion presentacion = merchandising("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = retirarStock.ejecutar("pres-1", new Cantidad(5, "unidad"));

        assertEquals(new Cantidad(15, "unidad"), resultado.getCantidadDisponible());
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void noSePuedeRetirarMasDeLoDisponibleYElStockNoCambia() {
        Presentacion presentacion = merchandising("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> retirarStock.ejecutar("pres-1", new Cantidad(21, "unidad")));

        assertEquals(new Cantidad(20, "unidad"), presentacion.getCantidadDisponible());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaPresentacionNoExisteYNoGuardaNada() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> retirarStock.ejecutar("no-existe", new Cantidad(5, "unidad")));

        verify(repositorio, never()).guardar(any());
    }
}
