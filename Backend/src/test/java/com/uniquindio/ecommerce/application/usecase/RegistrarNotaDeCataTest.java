package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
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
class RegistrarNotaDeCataTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private RegistrarNotaDeCata registrarNota;

    @Test
    void registraLaNotaEnUnaPresentacionTrazableYLaGuarda() {
        Presentacion presentacion = cafeVerde("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = registrarNota.ejecutar("pres-1", new NotaDeCata(List.of("Chocolate", "Panela")));

        assertEquals(new NotaDeCata(List.of("Chocolate", "Panela")),
                ((PresentacionTrazable) resultado).getNotaDeCata());
        verify(repositorio).guardar(presentacion);
    }

    @Test
    void unArticuloDeMerchandisingNoAdmiteNotaDeCataYNoSeGuardaNada() {
        Presentacion presentacion = merchandising("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> registrarNota.ejecutar("pres-1", new NotaDeCata(List.of("Chocolate"))));

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaPresentacionNoExisteYNoGuardaNada() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> registrarNota.ejecutar("no-existe", new NotaDeCata(List.of("Chocolate", "Panela"))));

        verify(repositorio, never()).guardar(any());
    }
}
