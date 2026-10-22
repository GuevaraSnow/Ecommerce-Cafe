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

/** Usa el ServicioDeCompra real: así la prueba cubre el descuento de stock y no solo la delegación. */
@ExtendWith(MockitoExtension.class)
class ConfirmarCompraTest {

    private static final DireccionDeEnvio DIRECCION = new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez");

    @Mock
    private CompraRepositorio compraRepositorio;

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    private ConfirmarCompra confirmarCompra;
    private Presentacion presentacion;
    private Compra carrito;

    @BeforeEach
    void preparar() {
        confirmarCompra = new ConfirmarCompra(compraRepositorio, new ServicioDeCompra(presentacionRepositorio));
        presentacion = cafeVerde("pres-1");
        carrito = Compra.abrirCarrito("compra-1", "comprador-1");
    }

    @Test
    void confirmaElCarritoDescuentaElStockYGuardaLaCompra() {
        carrito.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(50, "kg"));
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        confirmarCompra.ejecutar("compra-1", DIRECCION);

        assertEquals(EstadoDeCompra.CONFIRMADA, carrito.getEstado());
        assertEquals(DIRECCION, carrito.getDireccionEnvio().orElseThrow());
        assertEquals(new Cantidad(150, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio).guardar(carrito);
        verify(presentacionRepositorio).guardar(presentacion);
    }

    @Test
    void siOtroCompradorDejoElStockCortoNoSeConfirmaNiSeGuardaNada() {
        carrito.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(50, "kg"));
        presentacion.descontarCantidad(new Cantidad(180, "kg"));
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> confirmarCompra.ejecutar("compra-1", DIRECCION));

        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
        assertEquals(new Cantidad(20, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio, never()).guardar(any());
        verify(presentacionRepositorio, never()).guardar(any());
    }

    @Test
    void noSePuedeConfirmarDosVecesLaMismaCompra() {
        carrito.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(50, "kg"));
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));
        confirmarCompra.ejecutar("compra-1", DIRECCION);
        clearInvocations(compraRepositorio, presentacionRepositorio);

        assertThrows(ReglaDominioException.class, () -> confirmarCompra.ejecutar("compra-1", DIRECCION));

        assertEquals(new Cantidad(150, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaCompraNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> confirmarCompra.ejecutar("no-existe", DIRECCION));

        verify(compraRepositorio, never()).guardar(any());
    }
}
