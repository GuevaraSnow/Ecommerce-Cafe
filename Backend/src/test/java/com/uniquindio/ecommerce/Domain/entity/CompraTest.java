package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompraTest {

    private DireccionDeEnvio direccionValida() {
        return new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez");
    }

    private Compra compraValida() {
        return Compra.iniciar("comprador-1", direccionValida());
    }

    @Test
    void dosComprasConLosMismosDatosNoSonLaMismaPorqueElIdEsUnico() {
        Compra primera = compraValida();
        Compra segunda = compraValida();

        assertNotEquals(primera, segunda);
        assertNotEquals(primera.getId(), segunda.getId());
    }

    @Test
    void noSePuedeIniciarUnaCompraSinComprador() {
        assertThrows(ReglaDominioException.class, () -> Compra.iniciar("   ", direccionValida()));
    }

    @Test
    void noSePuedeIniciarUnaCompraSinDireccionDeEnvio() {
        assertThrows(ReglaDominioException.class, () -> Compra.iniciar("comprador-1", null));
    }

    @Test
    void unaCompraRecienIniciadaEstaPendienteYSinDetalles() {
        Compra compra = compraValida();

        assertEquals(EstadoDeCompra.PENDIENTE, compra.getEstado());
        assertTrue(compra.getDetalles().isEmpty());
        assertTrue(compra.getFechaEntrega().isEmpty());
    }

    @Test
    void agregarDetalleLoAsociaALaCompraYCongelaElPrecio() {
        Compra compra = compraValida();

        compra.agregarDetalle("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

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
        compra.agregarDetalle("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertThrows(UnsupportedOperationException.class, () -> compra.getDetalles().clear());
        assertEquals(1, compra.getDetalles().size());
    }
}