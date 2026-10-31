package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** La consulta solo delega en el repositorio y nunca guarda. */
@ExtendWith(MockitoExtension.class)
class ConsultarResenasDePresentacionTest {

    @Mock
    private ResenaRepositorio repositorio;

    @InjectMocks
    private ConsultarResenasDePresentacion consultar;

    @Test
    void devuelveLasResenasDeLaPresentacion() {
        List<Resena> resenas = List.of(Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1",
                new Calificacion(5), new TextoDeResena("Excelente"), LocalDateTime.of(2026, 10, 20, 9, 0)));
        when(repositorio.buscarPorPresentacion("pres-1")).thenReturn(resenas);

        assertEquals(resenas, consultar.ejecutar("pres-1"));
        verify(repositorio, never()).guardar(any());
    }
}
