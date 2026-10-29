package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import com.uniquindio.ecommerce.application.dto.response.ReembolsoResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReembolsoMapperTest {

    private final ReembolsoMapper mapper = new ReembolsoMapper();

    private static final LocalDateTime SOLICITADA = LocalDateTime.of(2026, 10, 21, 10, 0);

    private SolicitudReembolso solicitud() {
        return SolicitudReembolso.solicitar("reembolso-1", "compra-1", "pres-1", "vendedor-1",
                "Llego humedo", SOLICITADA);
    }

    @Test
    void unaSolicitudPendienteSeMapeaSinFechaDeResolucion() {
        ReembolsoResponse response = mapper.toResponse(solicitud());

        assertEquals("reembolso-1", response.id());
        assertEquals("compra-1", response.compraId());
        assertEquals("pres-1", response.presentacionId());
        assertEquals("vendedor-1", response.vendedorId());
        assertEquals("Llego humedo", response.motivo());
        assertEquals(EstadoDeReembolso.SOLICITADO, response.estado());
        assertEquals(SOLICITADA, response.fechaSolicitud());
        assertNull(response.fechaResolucion());
    }

    @Test
    void unaSolicitudAprobadaLlevaSuEstadoYFechaDeResolucion() {
        SolicitudReembolso aprobada = solicitud();
        LocalDateTime resuelta = SOLICITADA.plusHours(3);
        aprobada.aprobar("vendedor-1", resuelta);

        ReembolsoResponse response = mapper.toResponse(aprobada);

        assertEquals(EstadoDeReembolso.APROBADO, response.estado());
        assertEquals(resuelta, response.fechaResolucion());
    }
}
