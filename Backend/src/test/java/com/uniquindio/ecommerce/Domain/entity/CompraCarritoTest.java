package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
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

    @Test
    void cambiarCantidadConservaElPrecioCongelado() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        carrito.cambiarCantidad("presentacion-1", new Cantidad(5, "kg"));

        assertEquals(List.of(new DetalleDeCompra("compra-1", "presentacion-1",
                new Precio(50000, "COP"), new Cantidad(5, "kg"))), carrito.getDetalles());
    }

    @Test
    void noSePuedeCambiarLaCantidadAceroNiAOtraUnidadYElCarritoNoCambia() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertThrows(ReglaDominioException.class, () -> carrito.cambiarCantidad("presentacion-1", new Cantidad(0, "kg")));
        assertThrows(ReglaDominioException.class, () -> carrito.cambiarCantidad("presentacion-1", new Cantidad(500, "g")));
        assertThrows(ReglaDominioException.class, () -> carrito.cambiarCantidad("presentacion-1", null));

        assertEquals(new Cantidad(2, "kg"), carrito.getDetalles().get(0).cantidad());
    }

    @Test
    void noSePuedeCambiarLaCantidadDeUnaPresentacionQueNoEstaEnElCarrito() {
        assertThrows(ReglaDominioException.class, () -> carrito.cambiarCantidad("presentacion-9", new Cantidad(1, "kg")));

        assertTrue(carrito.getDetalles().isEmpty());
    }

    @Test
    void quitarPresentacionLaSacaDelCarritoYDejaLasDemas() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        carrito.agregarPresentacion("presentacion-2", new Precio(30000, "COP"), new Cantidad(1, "kg"));

        carrito.quitarPresentacion("presentacion-1");

        assertFalse(carrito.incluye("presentacion-1"));
        assertTrue(carrito.incluye("presentacion-2"));
    }

    @Test
    void noSePuedeQuitarUnaPresentacionQueNoEstaEnElCarrito() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertThrows(ReglaDominioException.class, () -> carrito.quitarPresentacion("presentacion-9"));

        assertEquals(1, carrito.getDetalles().size());
    }

    @Test
    void unaCompraConfirmadaNoPermiteCambiarCantidadesNiQuitarPresentaciones() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        carrito.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));

        assertThrows(ReglaDominioException.class, () -> carrito.cambiarCantidad("presentacion-1", new Cantidad(5, "kg")));
        assertThrows(ReglaDominioException.class, () -> carrito.quitarPresentacion("presentacion-1"));

        assertEquals(List.of(new DetalleDeCompra("compra-1", "presentacion-1",
                new Precio(50000, "COP"), new Cantidad(2, "kg"))), carrito.getDetalles());
    }

    @Test
    void cantidadDeDevuelveLoQueYaHayEnElCarritoDeEsaPresentacion() {
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        carrito.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(1, "kg"));

        assertEquals(new Cantidad(3, "kg"), carrito.cantidadDe("presentacion-1").orElseThrow());
        assertTrue(carrito.cantidadDe("presentacion-9").isEmpty());
    }
}
