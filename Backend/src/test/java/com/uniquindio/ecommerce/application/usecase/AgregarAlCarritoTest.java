package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
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

/** Usa el ServicioDeCompra real: así la prueba cubre la regla de stock y no solo la delegación. */
@ExtendWith(MockitoExtension.class)
class AgregarAlCarritoTest {

    @Mock
    private CompraRepositorio compraRepositorio;

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    private AgregarAlCarrito agregarAlCarrito;
    private Compra carrito;

    @BeforeEach
    void preparar() {
        agregarAlCarrito = new AgregarAlCarrito(compraRepositorio, presentacionRepositorio,
                new ServicioDeCompra(presentacionRepositorio));
        carrito = Compra.abrirCarrito("compra-1", "comprador-1");
    }

    @Test
    void agregaLaPresentacionAlCarritoYLoGuardaSinTocarElStock() {
        Presentacion presentacion = cafeVerde("pres-1");
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        agregarAlCarrito.ejecutar("compra-1", "pres-1", new Cantidad(10, "kg"));

        assertEquals(new Cantidad(10, "kg"), carrito.cantidadDe("pres-1").orElseThrow());
        assertEquals(new Cantidad(200, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio).guardar(carrito);
        verify(presentacionRepositorio, never()).guardar(any());
    }

    @Test
    void siElStockNoAlcanzaNoSeAgregaNiSeGuardaNada() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(cafeVerde("pres-1")));

        assertThrows(ReglaDominioException.class,
                () -> agregarAlCarrito.ejecutar("compra-1", "pres-1", new Cantidad(201, "kg")));

        assertTrue(carrito.getDetalles().isEmpty());
        verify(compraRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiElCarritoNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> agregarAlCarrito.ejecutar("no-existe", "pres-1", new Cantidad(1, "kg")));

        verify(compraRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaPresentacionNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(presentacionRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> agregarAlCarrito.ejecutar("compra-1", "no-existe", new Cantidad(1, "kg")));

        verify(compraRepositorio, never()).guardar(any());
    }
}
