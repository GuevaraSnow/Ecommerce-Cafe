package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.service.VerificadorDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.cafeVerde;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DejarResenaTest {

    private static final Calificacion CINCO = new Calificacion(5);
    private static final TextoDeResena TEXTO = new TextoDeResena("Notas a panela y buen cuerpo");

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    @Mock
    private ResenaRepositorio resenaRepositorio;

    @Mock
    private VerificadorDeCompra verificadorDeCompra;

    @InjectMocks
    private DejarResena dejarResena;

    @Test
    void dejaLaResenaConElVendedorDeLaPresentacionYLaGuarda() {
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(cafeVerde("pres-1")));

        Resena resena = dejarResena.ejecutar("resena-1", "comprador-1", "pres-1", CINCO, TEXTO);

        assertEquals("vendedor-1", resena.getVendedorId());
        assertEquals(CINCO, resena.getCalificacion());
        verify(verificadorDeCompra).verificarQuePuedeResenar("comprador-1", "pres-1");
        verify(resenaRepositorio).guardar(resena);
    }

    @Test
    void siElCompradorNoPuedeResenarNoSeGuardaNada() {
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(cafeVerde("pres-1")));
        doThrow(new ReglaDominioException("Solo puede resenar quien la adquirio"))
                .when(verificadorDeCompra).verificarQuePuedeResenar("comprador-1", "pres-1");

        assertThrows(ReglaDominioException.class,
                () -> dejarResena.ejecutar("resena-1", "comprador-1", "pres-1", CINCO, TEXTO));

        verify(resenaRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaPresentacionNoExisteYNoGuardaNada() {
        when(presentacionRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> dejarResena.ejecutar("resena-1", "comprador-1", "no-existe", CINCO, TEXTO));

        verify(resenaRepositorio, never()).guardar(any());
    }
}
