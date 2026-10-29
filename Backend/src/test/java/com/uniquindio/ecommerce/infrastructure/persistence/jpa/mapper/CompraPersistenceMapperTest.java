package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CompraEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** El mapper no pierde nada en el ida y vuelta dominio → tabla → dominio. Sin Spring. */
class CompraPersistenceMapperTest {

    private static final DireccionDeEnvio DIRECCION = new DireccionDeEnvio("Armenia", "Cra 14 # 10-20", "Ana Gomez");
    private static final LocalDateTime ENTREGA = LocalDateTime.of(2026, 10, 20, 15, 30);

    private final CompraPersistenceMapper mapper = new CompraPersistenceMapper();

    private Compra compraEntregada() {
        Compra compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(2, "kg"));
        compra.agregarPresentacion("pres-2", new Precio(15000, "COP"), new Cantidad(3, "unidad"));
        compra.confirmar(DIRECCION);
        compra.enviar();
        compra.entregar(ENTREGA);
        return compra;
    }

    @Test
    void elIdaYVueltaConservaLosDetallesYElPrecioCongelado() {
        Compra original = compraEntregada();

        Compra reconstruida = mapper.toDomain(mapper.toEntity(original));

        assertEquals(original.getDetalles(), reconstruida.getDetalles());
        assertEquals(new Precio(28000, "COP"), reconstruida.getDetalles().get(0).precioCongelado());
        assertEquals(new Cantidad(3, "unidad"), reconstruida.getDetalles().get(1).cantidad());
    }

    @Test
    void elIdaYVueltaConservaEstadoDireccionYFechas() {
        Compra original = compraEntregada();

        Compra reconstruida = mapper.toDomain(mapper.toEntity(original));

        assertEquals(original.getId(), reconstruida.getId());
        assertEquals("comprador-1", reconstruida.getCompradorId());
        assertEquals(original.getFechaCompra(), reconstruida.getFechaCompra());
        assertEquals(EstadoDeCompra.ENTREGADA, reconstruida.getEstado());
        assertEquals(DIRECCION, reconstruida.getDireccionEnvio().orElseThrow());
        assertEquals(ENTREGA, reconstruida.getFechaEntrega().orElseThrow());
    }

    @Test
    void unCarritoSinDireccionNiEntregaSeReconstruyeSinEllas() {
        Compra carrito = Compra.abrirCarrito("compra-2", "comprador-1");
        carrito.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(1, "kg"));

        CompraEntity entity = mapper.toEntity(carrito);
        Compra reconstruido = mapper.toDomain(entity);

        assertNull(entity.getDireccionEnvio());
        assertEquals(EstadoDeCompra.PENDIENTE, reconstruido.getEstado());
        assertTrue(reconstruido.getDireccionEnvio().isEmpty());
        assertTrue(reconstruido.getFechaEntrega().isEmpty());
        assertEquals(1, reconstruido.getDetalles().size());
    }

    @Test
    void laEntidadGuardaUnaFilaDeDetallePorPresentacion() {
        CompraEntity entity = mapper.toEntity(compraEntregada());

        assertEquals(2, entity.getDetalles().size());
        assertEquals("pres-2", entity.getDetalles().get(1).getPresentacionId());
        assertEquals(15000, entity.getDetalles().get(1).getPrecioCongelado().getMonto());
    }
}
