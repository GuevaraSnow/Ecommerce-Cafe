package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReposicion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReponerStockDePresentacionTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @Mock
    private ServicioDeReposicion servicioDeReposicion;

    @InjectMocks
    private ReponerStockDePresentacion reponerStock;

    @Test
    void delegaLaReposicionEnElServicioYGuardaLaPresentacion() {
        Presentacion presentacion = cafeVerde("pres-1");
        Cantidad cantidad = new Cantidad(30, "kg");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = reponerStock.ejecutar("pres-1", cantidad);

        assertSame(presentacion, resultado);
        verify(servicioDeReposicion).reponer(presentacion, cantidad);
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void noGuardaNadaSiElServicioRechazaLaReposicion() {
        Presentacion presentacion = cafeVerde("pres-1");
        Cantidad cantidad = new Cantidad(30, "kg");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));
        doThrow(new ReglaDominioException("el Lote no alcanza")).when(servicioDeReposicion)
                .reponer(presentacion, cantidad);

        assertThrows(ReglaDominioException.class, () -> reponerStock.ejecutar("pres-1", cantidad));

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void lanzaExcepcionSiLaPresentacionNoExisteYNoLlamaAlServicio() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(ReglaDominioException.class,
                () -> reponerStock.ejecutar("no-existe", new Cantidad(30, "kg")));

        verify(servicioDeReposicion, never()).reponer(any(), any());
        verify(repositorio, never()).guardar(any());
    }
}
