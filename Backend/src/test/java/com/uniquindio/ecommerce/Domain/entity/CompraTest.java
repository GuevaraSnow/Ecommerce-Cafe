package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompraTest {

    private DireccionDeEnvio direccionValida() {
        return new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez");
    }

    private Compra compraValida() {
        return Compra.abrirCarrito(java.util.UUID.randomUUID().toString(), "comprador-1");
    }

    @Test
    void dosCarritosDelMismoCompradorConDistintoIdNoSonLaMismaCompra() {
        Compra primera = Compra.abrirCarrito("compra-1", "comprador-1");
        Compra segunda = Compra.abrirCarrito("compra-2", "comprador-1");

        assertNotEquals(primera, segunda);
    }

    @Test
    void noSePuedeAbrirUnCarritoSinComprador() {
        assertThrows(ReglaDominioException.class, () -> Compra.abrirCarrito("compra-1", "   "));
    }

    @Test
    void noSePuedeAbrirUnCarritoSinIdentificador() {
        assertThrows(ReglaDominioException.class, () -> Compra.abrirCarrito(null, "comprador-1"));
        assertThrows(ReglaDominioException.class, () -> Compra.abrirCarrito(" ", "comprador-1"));
    }

    @Test
    void unCarritoRecienAbiertoEstaPendienteSinDetallesNiDireccion() {
        Compra compra = compraValida();

        assertEquals(EstadoDeCompra.PENDIENTE, compra.getEstado());
        assertTrue(compra.getDetalles().isEmpty());
        assertTrue(compra.getDireccionEnvio().isEmpty());
        assertTrue(compra.getFechaEntrega().isEmpty());
    }

    @Test
    void agregarPresentacionLaAsociaALaCompraYCongelaElPrecio() {
        Compra compra = compraValida();

        compra.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertEquals(1, compra.getDetalles().size());
        DetalleDeCompra detalle = compra.getDetalles().get(0);
        assertEquals(compra.getId(), detalle.compraId());
        assertEquals("presentacion-1", detalle.presentacionId());
        assertEquals(new Precio(50000, "COP"), detalle.precioCongelado());
        assertEquals(new Cantidad(2, "kg"), detalle.cantidad());
    }

    @Test
    void laListaDeDetallesQueSeExponeNoPermiteModificarLaCompraDesdeAfuera() {
        Compra compra = compraValida();
        compra.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertThrows(UnsupportedOperationException.class, () -> compra.getDetalles().clear());
        assertEquals(1, compra.getDetalles().size());
    }

    @Test
    void incluyeIndicaSiLaPresentacionEstaEnLosDetalles() {
        Compra compra = compraValida();
        compra.agregarPresentacion("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertTrue(compra.incluye("presentacion-1"));
        assertFalse(compra.incluye("presentacion-2"));
    }

    @Test
    void reconstruirDevuelveLaCompraTalComoSeGuardo() {
        DetalleDeCompra detalle = new DetalleDeCompra("compra-1", "presentacion-1",
                new Precio(50000, "COP"), new Cantidad(2, "kg"));
        LocalDateTime entregada = LocalDateTime.of(2026, 10, 18, 9, 0);

        Compra compra = Compra.reconstruir("compra-1", "comprador-1", LocalDate.of(2026, 10, 15),
                direccionValida(), List.of(detalle), EstadoDeCompra.ENTREGADA, entregada);

        assertEquals("compra-1", compra.getId());
        assertEquals("comprador-1", compra.getCompradorId());
        assertEquals(LocalDate.of(2026, 10, 15), compra.getFechaCompra());
        assertEquals(direccionValida(), compra.getDireccionEnvio().orElseThrow());
        assertEquals(List.of(detalle), compra.getDetalles());
        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
        assertEquals(entregada, compra.getFechaEntrega().orElseThrow());
    }
}
