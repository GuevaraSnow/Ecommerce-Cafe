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

    private CompraFisica compraValida() {
        return CompraFisica.iniciar("comprador-1", direccionValida());
    }

    @Test
    void dosComprasConLosMismosDatosNoSonLaMismaPorqueElIdEsUnico() {
        CompraFisica primera = compraValida();
        CompraFisica segunda = compraValida();

        assertNotEquals(primera, segunda);
        assertNotEquals(primera.getId(), segunda.getId());
    }

    @Test
    void noSePuedeIniciarUnaCompraSinComprador() {
        assertThrows(ReglaDominioException.class, () -> CompraFisica.iniciar("   ", direccionValida()));
    }

    @Test
    void noSePuedeIniciarUnaCompraSinDireccionDeEnvio() {
        assertThrows(ReglaDominioException.class, () -> CompraFisica.iniciar("comprador-1", null));
    }

    @Test
    void unaCompraRecienIniciadaEstaPendienteYSinDetalles() {
        CompraFisica compra = compraValida();

        assertEquals(EstadoDeCompra.PENDIENTE, compra.getEstado());
        assertTrue(compra.getDetalles().isEmpty());
        assertTrue(compra.getFechaEntrega().isEmpty());
    }

    @Test
    void agregarDetalleLoAsociaALaCompraYCongelaElPrecio() {
        CompraFisica compra = compraValida();

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
        CompraFisica compra = compraValida();
        compra.agregarDetalle("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertThrows(UnsupportedOperationException.class, () -> compra.getDetalles().clear());
        assertEquals(1, compra.getDetalles().size());
    }

    @Test
    void incluyeIndicaSiLaPresentacionEstaEnLosDetalles() {
        CompraFisica compra = compraValida();
        compra.agregarDetalle("presentacion-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        assertTrue(compra.incluye("presentacion-1"));
        assertFalse(compra.incluye("presentacion-2"));
    }
}