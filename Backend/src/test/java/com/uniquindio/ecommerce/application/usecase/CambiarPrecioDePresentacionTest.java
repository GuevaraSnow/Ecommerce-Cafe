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
class CambiarPrecioDePresentacionTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private CambiarPrecioDePresentacion cambiarPrecio;

    @Test
    void cambiaElPrecioYGuardaLaPresentacion() {
        Presentacion presentacion = merchandising("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = cambiarPrecio.ejecutar("pres-1", new Precio(18000, "COP"));

        assertEquals(new Precio(18000, "COP"), resultado.getPrecio());
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void noGuardaNadaSiLaPresentacionFueDadaDeBaja() {
        Presentacion presentacion = merchandising("pres-1");
        presentacion.darDeBaja();
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> cambiarPrecio.ejecutar("pres-1", new Precio(18000, "COP")));

        assertEquals(new Precio(15000, "COP"), presentacion.getPrecio());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void lanzaExcepcionSiLaPresentacionNoExisteYNoGuardaNada() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(ReglaDominioException.class, () -> cambiarPrecio.ejecutar("no-existe", new Precio(18000, "COP")));

        verify(repositorio, never()).guardar(any());
    }
}
