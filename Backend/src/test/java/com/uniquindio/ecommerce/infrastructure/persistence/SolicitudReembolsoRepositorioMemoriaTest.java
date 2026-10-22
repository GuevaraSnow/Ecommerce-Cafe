package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** existeSolicitudVigente: cuenta las SOLICITADAS y APROBADAS de esa Compra y Presentación, no las rechazadas. */
class SolicitudReembolsoRepositorioMemoriaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 21, 10, 0);

    private final SolicitudReembolsoRepositorioMemoria repositorio = new SolicitudReembolsoRepositorioMemoria();

    private SolicitudReembolso solicitud(String id, String compraId, String presentacionId) {
        SolicitudReembolso solicitud = SolicitudReembolso.solicitar(id, compraId, presentacionId, "vendedor-1",
                "Llego humedo", AHORA);
        repositorio.guardar(solicitud);
        return solicitud;
    }

    @Test
    void unaSolicitudPendienteEsVigente() {
        solicitud("reembolso-1", "compra-1", "pres-1");

        assertTrue(repositorio.existeSolicitudVigente("compra-1", "pres-1"));
    }

    @Test
    void unaSolicitudAprobadaEsVigente() {
        solicitud("reembolso-1", "compra-1", "pres-1").aprobar("vendedor-1", AHORA.plusHours(1));

        assertTrue(repositorio.existeSolicitudVigente("compra-1", "pres-1"));
    }

    @Test
    void unaSolicitudRechazadaNoEsVigente() {
        solicitud("reembolso-1", "compra-1", "pres-1").rechazar("vendedor-1", AHORA.plusHours(1));

        assertFalse(repositorio.existeSolicitudVigente("compra-1", "pres-1"));
    }

    @Test
    void existeSolicitudAprobadaSoloCuentaLasAprobadas() {
        solicitud("reembolso-1", "compra-1", "pres-1");
        assertFalse(repositorio.existeSolicitudAprobada("compra-1", "pres-1"));

        repositorio.buscarPorId("reembolso-1").orElseThrow().aprobar("vendedor-1", AHORA.plusHours(1));

        assertTrue(repositorio.existeSolicitudAprobada("compra-1", "pres-1"));
        assertFalse(repositorio.existeSolicitudAprobada("compra-1", "pres-2"));
    }

    @Test
    void noCuentaLasSolicitudesDeOtraCompraNiDeOtraPresentacion() {
        solicitud("reembolso-1", "compra-2", "pres-1");
        solicitud("reembolso-2", "compra-1", "pres-2");

        assertFalse(repositorio.existeSolicitudVigente("compra-1", "pres-1"));
    }
}
