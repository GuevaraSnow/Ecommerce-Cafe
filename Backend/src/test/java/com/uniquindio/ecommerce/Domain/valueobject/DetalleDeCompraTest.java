package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DetalleDeCompraTest {

    private final Precio precio = new Precio(50000, "COP");
    private final Cantidad cantidad = new Cantidad(2, "kg");

    @Test
    void dosDetallesConLosMismosValoresSonIntercambiables() {
        DetalleDeCompra primero = new DetalleDeCompra("compra-1", "presentacion-1", precio, cantidad);
        DetalleDeCompra segundo = new DetalleDeCompra("compra-1", "presentacion-1", precio, cantidad);

        assertEquals(primero, segundo);
    }

    @Test
    void noSePuedeCrearUnDetalleSinCompra() {
        assertThrows(ReglaDominioException.class,
                () -> new DetalleDeCompra("   ", "presentacion-1", precio, cantidad));
    }

    @Test
    void noSePuedeCrearUnDetalleSinPresentacion() {
        assertThrows(ReglaDominioException.class,
                () -> new DetalleDeCompra("compra-1", null, precio, cantidad));
    }

    @Test
    void noSePuedeCrearUnDetalleSinPrecio() {
        assertThrows(ReglaDominioException.class,
                () -> new DetalleDeCompra("compra-1", "presentacion-1", null, cantidad));
    }

    @Test
    void noSePuedeCrearUnDetalleSinCantidad() {
        assertThrows(ReglaDominioException.class,
                () -> new DetalleDeCompra("compra-1", "presentacion-1", precio, null));
    }

    @Test
    void noSePuedeComprarUnaCantidadEnCero() {
        assertThrows(ReglaDominioException.class,
                () -> new DetalleDeCompra("compra-1", "presentacion-1", precio, new Cantidad(0, "kg")));
    }
}