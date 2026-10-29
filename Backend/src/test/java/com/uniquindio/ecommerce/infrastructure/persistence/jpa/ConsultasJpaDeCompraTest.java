package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Las consultas existsBy de los adaptadores JPA, contra H2 con los datos de data-compra.sql.
 * Se prueban a través de los puertos del dominio, que es como las usan los Domain Services.
 */
@SpringBootTest
class ConsultasJpaDeCompraTest {

    @Autowired
    private CompraRepositorio compraRepositorio;

    @Autowired
    private SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;

    @Autowired
    private ResenaRepositorio resenaRepositorio;

    @Test
    void unaPresentacionEnUnaCompraPendienteConfirmadaOEnviadaTieneComprasActivas() {
        assertTrue(compraRepositorio.existeCompraActivaConPresentacion("pres-cafe-2"));  // compra-002 PENDIENTE
        assertTrue(compraRepositorio.existeCompraActivaConPresentacion("pres-cafe-1"));  // compra-003 CONFIRMADA
        assertTrue(compraRepositorio.existeCompraActivaConPresentacion("pres-cafe-3"));  // compra-004 ENVIADA
    }

    @Test
    void unaPresentacionSoloEnComprasEntregadasNoTieneComprasActivas() {
        assertFalse(compraRepositorio.existeCompraActivaConPresentacion("pres-taza-1")); // solo compra-001 ENTREGADA
        assertFalse(compraRepositorio.existeCompraActivaConPresentacion("pres-no-existe"));
    }

    @Test
    void existeCompraEntregadaCuandoEseCompradorRecibioEsaPresentacion() {
        assertTrue(compraRepositorio.existeCompraEntregada("usr-ana", "pres-cafe-1"));
        assertTrue(compraRepositorio.existeCompraEntregada("usr-diego", "pres-cafe-2"));
    }

    @Test
    void noCuentaUnaCompraQueNoEstaEntregadaNiLaDeOtroComprador() {
        assertFalse(compraRepositorio.existeCompraEntregada("usr-ana", "pres-cafe-2"));   // compra-002 PENDIENTE
        assertFalse(compraRepositorio.existeCompraEntregada("usr-carla", "pres-cafe-3")); // compra-006 REEMBOLSADA
        assertFalse(compraRepositorio.existeCompraEntregada("usr-bruno", "pres-cafe-1")); // la de Ana no es de Bruno
    }

    @Test
    void unaSolicitudPendienteOAprobadaEsVigenteYUnaRechazadaNo() {
        assertTrue(solicitudReembolsoRepositorio.existeSolicitudVigente("compra-007", "pres-cafe-2"));  // SOLICITADO
        assertTrue(solicitudReembolsoRepositorio.existeSolicitudVigente("compra-007", "pres-cafe-3"));  // APROBADO
        assertFalse(solicitudReembolsoRepositorio.existeSolicitudVigente("compra-001", "pres-taza-1")); // RECHAZADO
    }

    @Test
    void soloCuentaComoAprobadaLaSolicitudAprobada() {
        assertTrue(solicitudReembolsoRepositorio.existeSolicitudAprobada("compra-006", "pres-cafe-3"));
        assertFalse(solicitudReembolsoRepositorio.existeSolicitudAprobada("compra-007", "pres-cafe-2")); // SOLICITADO
    }

    @Test
    void existeResenaSoloDelCompradorQueYaResenoEsaPresentacion() {
        assertTrue(resenaRepositorio.existeResenaDe("usr-ana", "pres-cafe-1"));
        assertFalse(resenaRepositorio.existeResenaDe("usr-diego", "pres-cafe-2"));
        assertFalse(resenaRepositorio.existeResenaDe("usr-bruno", "pres-cafe-1"));
    }

    @Test
    void buscarPorIdTraeLaCompraConSusDetallesDesdeH2() {
        Compra compra = compraRepositorio.buscarPorId("compra-007").orElseThrow();

        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
        assertEquals(2, compra.getDetalles().size());
        assertTrue(compra.getFechaEntrega().isPresent());
        assertTrue(compraRepositorio.buscarPorId("compra-no-existe").isEmpty());
    }
}
