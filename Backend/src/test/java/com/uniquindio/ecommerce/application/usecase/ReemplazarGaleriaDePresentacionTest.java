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
class ReemplazarGaleriaDePresentacionTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private ReemplazarGaleriaDePresentacion reemplazarGaleria;

    @Test
    void reemplazaLaGaleriaYGuardaLaPresentacion() {
        Presentacion presentacion = merchandising("pres-1");
        Galeria nueva = new Galeria(List.of(new ImagenDePresentacion("http://img/nueva.png", true)));
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = reemplazarGaleria.ejecutar("pres-1", nueva);

        assertEquals(nueva, resultado.getGaleria());
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void noGuardaNadaSiLaPresentacionFueDadaDeBaja() {
        Presentacion presentacion = merchandising("pres-1");
        presentacion.darDeBaja();
        Galeria nueva = new Galeria(List.of(new ImagenDePresentacion("http://img/nueva.png", true)));
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> reemplazarGaleria.ejecutar("pres-1", nueva));

        assertEquals(galeria(), presentacion.getGaleria());
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaPresentacionNoExisteYNoGuardaNada() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> reemplazarGaleria.ejecutar("no-existe", galeria()));

        verify(repositorio, never()).guardar(any());
    }
}
