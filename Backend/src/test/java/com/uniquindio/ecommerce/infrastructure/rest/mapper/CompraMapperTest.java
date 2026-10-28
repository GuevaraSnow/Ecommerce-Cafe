package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.application.dto.request.CantidadRequest;
import com.uniquindio.ecommerce.application.dto.request.DireccionDeEnvioRequest;
import com.uniquindio.ecommerce.application.dto.response.CantidadResponse;
import com.uniquindio.ecommerce.application.dto.response.CompraResponse;
import com.uniquindio.ecommerce.application.dto.response.DetalleDeCompraResponse;
import com.uniquindio.ecommerce.application.dto.response.DireccionDeEnvioResponse;
import com.uniquindio.ecommerce.application.dto.response.PrecioResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Sin Spring: el mapper solo traduce, las reglas las valida el dominio. */
class CompraMapperTest {

    private static final LocalDateTime ENTREGA = LocalDateTime.of(2026, 10, 20, 15, 30);

    private final CompraMapper mapper = new CompraMapper();

    @Test
    void unaCompraEntregadaSeAplanaConSusDetallesPrecioCongeladoYDireccion() {
        Compra compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(2, "kg"));
        compra.confirmar(new DireccionDeEnvio("Armenia", "Cra 14 # 10-20", "Ana Gomez"));
        compra.enviar();
        compra.entregar(ENTREGA);

        CompraResponse response = mapper.toResponse(compra);

        assertEquals("compra-1", response.id());
        assertEquals("comprador-1", response.compradorId());
        assertEquals(EstadoDeCompra.ENTREGADA, response.estado());
        assertEquals(List.of(new DetalleDeCompraResponse("pres-1", new PrecioResponse(28000, "COP"),
                new CantidadResponse(2, "kg"))), response.detalles());
        assertEquals(new DireccionDeEnvioResponse("Armenia", "Cra 14 # 10-20", "Ana Gomez"), response.direccionEnvio());
        assertEquals(ENTREGA, response.fechaEntrega());
    }

    @Test
    void unCarritoSaleSinDireccionNiFechaDeEntrega() {
        CompraResponse response = mapper.toResponse(Compra.abrirCarrito("compra-2", "comprador-1"));

        assertEquals(EstadoDeCompra.PENDIENTE, response.estado());
        assertTrue(response.detalles().isEmpty());
        assertNull(response.direccionEnvio());
        assertNull(response.fechaEntrega());
    }

    @Test
    void convierteLosRequestsEnValueObjects() {
        assertEquals(new Cantidad(2, "kg"), mapper.toCantidad(new CantidadRequest(2.0, "kg")));
        assertEquals(new DireccionDeEnvio("Armenia", "Cra 14", "Ana"),
                mapper.toDireccionDeEnvio(new DireccionDeEnvioRequest("Armenia", "Cra 14", "Ana")));
        assertNull(mapper.toCantidad(null));
        assertNull(mapper.toDireccionDeEnvio(null));
    }

    @Test
    void elMapperNoValidaReglasLasRechazaElValueObject() {
        assertThrows(ReglaDominioException.class, () -> mapper.toCantidad(new CantidadRequest(2.0, "litros")));
    }
}
