package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompraCarritoTest {

    private Compra carrito;

    @BeforeEach
    void abrirCarrito() {
        carrito = Compra.abrirCarrito("compra-1", "comprador-1");
    }

    @Test
    void agregarLaMismaPresentacionSumaLaCantidadYConservaElPrecioCongeladoPrimero() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        carrito.agregarPresentacion("presentacion-1", new Precio(45000, "COP"), new Cantidad(1, "kg"));

        assertEquals(List.of(new DetalleDeCompra("compra-1", "presentacion-1",
                new Precio(50000, "COP"), new Cantidad(3, "kg"))), carrito.getDetalles());
    }

    @Test
    void agregarPresentacionesDistintasCreaUnDetallePorCadaUna() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        carrito.agregarPresentacion("presentacion-2", new Precio(30000, "COP"), new Cantidad(1, "kg"));

        assertEquals(2, carrito.getDetalles().size());
    }

    @Test
    void noSePuedeSumarUnaCantidadConOtraUnidadYElCarritoNoCambia() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertThrows(ReglaDominioException.class,
                () -> carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(500, "g")));

        assertEquals(new Cantidad(2, "kg"), carrito.getDetalles().get(0).cantidad());
    }

    @Test
    void noSePuedeAgregarSinPrecioNiConCantidadCeroYElCarritoNoCambia() {
        assertThrows(ReglaDominioException.class,
                () -> carrito.agregarPresentacion("presentacion-1", null, new Cantidad(2, "kg")));
        assertThrows(ReglaDominioException.class,
                () -> carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(0, "kg")));

        assertTrue(carrito.getDetalles().isEmpty());
    }

    @Test
    void unaCompraCanceladaYaNoEsCarritoYNoAceptaPresentaciones() {
        carrito.cancelar();

        assertThrows(ReglaDominioException.class,
                () -> carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg")));

        assertTrue(carrito.getDetalles().isEmpty());
    }
}
