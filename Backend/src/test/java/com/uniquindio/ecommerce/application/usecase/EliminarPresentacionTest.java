package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeBajaDePresentacion;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EliminarPresentacionTest {

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    @Mock
    private ServicioDeBajaDePresentacion servicioDeBaja;

    @InjectMocks
    private EliminarPresentacion eliminarPresentacion;

    private Presentacion presentacion() {
        return Presentacion.publicar("pres-1", "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/manilla.png", true))), null,
                RolVendedor.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    @Test
    void delegaLaBajaEnElServicioYGuardaLaPresentacion() {
        Presentacion presentacion = presentacion();
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        Presentacion resultado = eliminarPresentacion.ejecutar("pres-1");

        assertSame(presentacion, resultado);
        verify(servicioDeBaja).darDeBaja(presentacion);
        verify(presentacionRepositorio).guardar(presentacion);
    }

    @Test
    void noGuardaNadaSiElServicioRechazaLaBaja() {
        Presentacion presentacion = presentacion();
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));
        doThrow(new ReglaDominioException("compras activas")).when(servicioDeBaja).darDeBaja(presentacion);

        assertThrows(ReglaDominioException.class, () -> eliminarPresentacion.ejecutar("pres-1"));

        verify(presentacionRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaExcepcionSiLaPresentacionNoExisteYNoLlamaAlServicio() {
        when(presentacionRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(ReglaDominioException.class, () -> eliminarPresentacion.ejecutar("no-existe"));

        verify(servicioDeBaja, never()).darDeBaja(any());
        verify(presentacionRepositorio, never()).guardar(any());
    }
}
