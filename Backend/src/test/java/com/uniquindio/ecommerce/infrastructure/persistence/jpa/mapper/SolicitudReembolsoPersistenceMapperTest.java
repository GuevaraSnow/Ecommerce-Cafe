package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** El mapper no pierde nada en el ida y vuelta dominio → tabla → dominio. Sin Spring. */
class SolicitudReembolsoPersistenceMapperTest {

    private static final LocalDateTime SOLICITADA = LocalDateTime.of(2026, 10, 21, 10, 0);
    private static final LocalDateTime RESUELTA = LocalDateTime.of(2026, 10, 21, 16, 30);

    private final SolicitudReembolsoPersistenceMapper mapper = new SolicitudReembolsoPersistenceMapper();

    @Test
    void unaSolicitudAprobadaConservaTodosSusDatos() {
        SolicitudReembolso original = SolicitudReembolso.solicitar("reembolso-1", "compra-1", "pres-1",
                "vendedor-1", "Llego humedo", SOLICITADA);
        original.aprobar("vendedor-1", RESUELTA);

        SolicitudReembolso reconstruida = mapper.toDomain(mapper.toEntity(original));

        assertEquals("reembolso-1", reconstruida.getId());
        assertEquals("compra-1", reconstruida.getCompraId());
        assertEquals("pres-1", reconstruida.getPresentacionId());
        assertEquals("vendedor-1", reconstruida.getVendedorId());
        assertEquals("Llego humedo", reconstruida.getMotivo());
        assertEquals(SOLICITADA, reconstruida.getFechaSolicitud());
        assertEquals(EstadoDeReembolso.APROBADO, reconstruida.getEstado());
        assertEquals(RESUELTA, reconstruida.getFechaResolucion().orElseThrow());
    }

    @Test
    void unaSolicitudPendienteSeReconstruyeSinFechaDeResolucionYSePuedeResolver() {
        SolicitudReembolso original = SolicitudReembolso.solicitar("reembolso-1", "compra-1", "pres-1",
                "vendedor-1", "Llego humedo", SOLICITADA);

        SolicitudReembolso reconstruida = mapper.toDomain(mapper.toEntity(original));

        assertEquals(EstadoDeReembolso.SOLICITADO, reconstruida.getEstado());
        assertTrue(reconstruida.getFechaResolucion().isEmpty());
        reconstruida.rechazar("vendedor-1", RESUELTA);
        assertEquals(EstadoDeReembolso.RECHAZADO, reconstruida.getEstado());
    }
}
