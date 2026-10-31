package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionVendida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** La consulta solo pide el ranking al repositorio con el límite y nunca guarda. */
@ExtendWith(MockitoExtension.class)
class ConsultarMasVendidasTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private ConsultarMasVendidas consultarMasVendidas;

    @Test
    void devuelveElRankingQueArmaElRepositorioConElLimite() {
        List<PresentacionVendida> ranking = List.of(new PresentacionVendida("pres-1", "Cafe", 3, 90000.0));
        when(repositorio.masVendidas(5)).thenReturn(ranking);

        assertSame(ranking, consultarMasVendidas.ejecutar(5));
        verify(repositorio, never()).guardar(any());
    }
}
