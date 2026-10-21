package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Descuento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.merchandising;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Programar y quitar el Descuento: los dos casos de uso solo cargan, delegan en la Presentación y guardan. */
@ExtendWith(MockitoExtension.class)
class DescuentoDePresentacionTest {

    private static final Descuento DESCUENTO = new Descuento(20, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private ProgramarDescuentoDePresentacion programar;

    @InjectMocks
    private QuitarDescuentoDePresentacion quitar;

    @Test
    void programaElDescuentoYGuardaLaPresentacion() {
        Presentacion presentacion = merchandising("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = programar.ejecutar("pres-1", DESCUENTO);

        assertEquals(DESCUENTO, resultado.getDescuento());
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void programarEnUnaPresentacionDadaDeBajaNoGuardaNada() {
        Presentacion presentacion = merchandising("pres-1");
        presentacion.darDeBaja();
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> programar.ejecutar("pres-1", DESCUENTO));

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void programarEnUnaPresentacionInexistenteLanzaNoEncontradoYNoGuardaNada() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> programar.ejecutar("no-existe", DESCUENTO));

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void quitaElDescuentoYGuardaLaPresentacion() {
        Presentacion presentacion = merchandising("pres-1");
        presentacion.programarDescuento(DESCUENTO);
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = quitar.ejecutar("pres-1");

        assertNull(resultado.getDescuento());
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void quitarEnUnaPresentacionInexistenteLanzaNoEncontradoYNoGuardaNada() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> quitar.ejecutar("no-existe"));

        verify(repositorio, never()).guardar(any());
    }
}
