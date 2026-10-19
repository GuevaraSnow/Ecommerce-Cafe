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
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PausarPresentacionTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private PausarPresentacion pausar;

    @Test
    void pausaLaPresentacionYLaGuarda() {
        Presentacion presentacion = merchandising("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = pausar.ejecutar("pres-1");

        assertEquals(EstadoDePublicacion.INACTIVA, resultado.getEstado());
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void noSePuedePausarUnaPresentacionAgotadaYNoGuardaNada() {
        Presentacion presentacion = merchandising("pres-1");
        presentacion.descontarCantidad(new Cantidad(20, "unidad"));
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> pausar.ejecutar("pres-1"));

        assertEquals(EstadoDePublicacion.AGOTADA, presentacion.getEstado());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void lanzaExcepcionSiLaPresentacionNoExisteYNoGuardaNada() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(ReglaDominioException.class, () -> pausar.ejecutar("no-existe"));

        verify(repositorio, never()).guardar(any());
    }
}
