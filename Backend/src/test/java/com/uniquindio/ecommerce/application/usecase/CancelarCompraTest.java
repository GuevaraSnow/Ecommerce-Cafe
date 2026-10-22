package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.cafeVerde;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Usa el ServicioDeCompra real: así la prueba cubre la reposición de stock y no solo la delegación. */
@ExtendWith(MockitoExtension.class)
class CancelarCompraTest {

    @Mock
    private CompraRepositorio compraRepositorio;

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    private CancelarCompra cancelarCompra;
    private Presentacion presentacion;
    private Compra compra;

    @BeforeEach
    void preparar() {
        cancelarCompra = new CancelarCompra(compraRepositorio, new ServicioDeCompra(presentacionRepositorio));
        presentacion = cafeVerde("pres-1");
        compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(50, "kg"));
    }

    private void confirmarYDescontar() {
        compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        presentacion.descontarCantidad(new Cantidad(50, "kg"));
    }

    @Test
    void cancelarUnaCompraConfirmadaReponeElStockYGuardaAmbas() {
        confirmarYDescontar();
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        cancelarCompra.ejecutar("compra-1");

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
        assertEquals(new Cantidad(200, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio).guardar(compra);
        verify(presentacionRepositorio).guardar(presentacion);
    }

    @Test
    void cancelarUnCarritoPendienteNoTocaElStock() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        cancelarCompra.ejecutar("compra-1");

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
        assertEquals(new Cantidad(200, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio).guardar(compra);
        verify(presentacionRepositorio, never()).guardar(any());
    }

    @Test
    void noSePuedeCancelarUnaCompraEnviadaYNoSeGuardaNada() {
        confirmarYDescontar();
        compra.enviar();
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar("compra-1"));

        assertEquals(EstadoDeCompra.ENVIADA, compra.getEstado());
        assertEquals(new Cantidad(150, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio, never()).guardar(any());
        verify(presentacionRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaCompraNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> cancelarCompra.ejecutar("no-existe"));

        verify(compraRepositorio, never()).guardar(any());
    }
}
