package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.SolicitudReembolsoRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioDeReembolsoTest {

    private static final LocalDateTime ENTREGA = LocalDateTime.of(2026, 10, 20, 15, 0);

    private final SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
    private final ServicioDeReembolso servicio = new ServicioDeReembolso(solicitudes);

    private Presentacion presentacion;
    private Compra compra;

    private Presentacion cafeVerde(String id, String vendedorId) {
        return Presentacion.publicar(id, vendedorId, "LOT-2026-001", null, "Cafe verde " + id,
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(10, "kg"),
                new Galeria(List.of(new ImagenDePresentacion("https://img.cafe/" + id + ".jpg", true))),
                null, RolVendedor.CAFICULTOR, null, null, null);
    }

    @BeforeEach
    void preparar() {
        presentacion = cafeVerde("pres-1", "vendedor-7");
        compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(2, "kg"));
        compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        compra.enviar();
        compra.entregar(ENTREGA);
    }

    @Test
    void creaLaSolicitudDeLaPresentacionConSuVendedor() {
        SolicitudReembolso solicitud = servicio.solicitar("reembolso-1", compra, "comprador-1", presentacion,
                "Llego humedo", ENTREGA.plusHours(3));

        assertEquals("compra-1", solicitud.getCompraId());
        assertEquals("pres-1", solicitud.getPresentacionId());
        assertEquals("vendedor-7", solicitud.getVendedorId());
        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
    }

    @Test
    void fueraDeLas48HorasNoSeCreaLaSolicitudYLaCompraNoCambia() {
        assertThrows(ReglaDominioException.class, () -> servicio.solicitar("reembolso-1", compra, "comprador-1",
                presentacion, "Llego humedo", ENTREGA.plusHours(49)));

        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
    }

    @Test
    void otroCompradorNoPuedePedirElReembolso() {
        assertThrows(ReglaDominioException.class, () -> servicio.solicitar("reembolso-1", compra, "comprador-2",
                presentacion, "Llego humedo", ENTREGA.plusHours(3)));
    }

    @Test
    void unaPresentacionQueNoEstaEnLaCompraNoSePuedeReembolsar() {
        assertThrows(ReglaDominioException.class, () -> servicio.solicitar("reembolso-1", compra, "comprador-1",
                cafeVerde("pres-9", "vendedor-7"), "Llego humedo", ENTREGA.plusHours(3)));
    }

    private SolicitudReembolso solicitarYGuardar() {
        SolicitudReembolso solicitud = servicio.solicitar("reembolso-1", compra, "comprador-1", presentacion,
                "Llego humedo", ENTREGA.plusHours(3));
        solicitudes.guardar(solicitud);
        return solicitud;
    }

    @Test
    void noSePuedePedirOtraVezMientrasHayUnaSolicitudPendiente() {
        solicitarYGuardar();

        assertThrows(ReglaDominioException.class, () -> servicio.solicitar("reembolso-2", compra, "comprador-1",
                presentacion, "Llego humedo", ENTREGA.plusHours(4)));
    }

    @Test
    void noSePuedePedirOtraVezSiYaFueAprobada() {
        solicitarYGuardar().aprobar("vendedor-7", ENTREGA.plusHours(5));

        assertThrows(ReglaDominioException.class, () -> servicio.solicitar("reembolso-2", compra, "comprador-1",
                presentacion, "Llego humedo", ENTREGA.plusHours(6)));
    }

    @Test
    void siLaRechazaronSePuedeVolverAPedirDentroDeLas48Horas() {
        solicitarYGuardar().rechazar("vendedor-7", ENTREGA.plusHours(5));

        SolicitudReembolso otraVez = servicio.solicitar("reembolso-2", compra, "comprador-1", presentacion,
                "Sigue humedo, envio fotos", ENTREGA.plusHours(6));

        assertEquals(EstadoDeReembolso.SOLICITADO, otraVez.getEstado());
    }

    /** Compra entregada con 4 Presentaciones; cada una con su solicitud de reembolso guardada. */
    private Compra compraDeCuatro() {
        Compra cuatro = Compra.abrirCarrito("compra-4", "comprador-1");
        for (int i = 1; i <= 4; i++) {
            cuatro.agregarPresentacion("pres-" + i, new Precio(28000, "COP"), new Cantidad(1, "kg"));
        }
        cuatro.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        cuatro.enviar();
        cuatro.entregar(ENTREGA);
        for (int i = 1; i <= 4; i++) {
            solicitudes.guardar(SolicitudReembolso.solicitar("reembolso-" + i, "compra-4", "pres-" + i,
                    "vendedor-" + i, "Llego humedo", ENTREGA.plusHours(2)));
        }
        return cuatro;
    }

    private void aprobarDe(Compra cuatro, int i) {
        servicio.aprobar(solicitudes.buscarPorId("reembolso-" + i).orElseThrow(), cuatro, "vendedor-" + i,
                ENTREGA.plusHours(10));
    }

    @Test
    void siLaCompraTieneUnaSolaPresentacionAprobarlaPasaLaCompraAReembolsada() {
        SolicitudReembolso solicitud = solicitarYGuardar();

        servicio.aprobar(solicitud, compra, "vendedor-7", ENTREGA.plusHours(5));

        assertEquals(EstadoDeReembolso.APROBADO, solicitud.getEstado());
        assertEquals(EstadoDeCompra.REEMBOLSADA, compra.getEstado());
    }

    @Test
    void conTresDeCuatroAprobadasLaCompraSigueEntregada() {
        Compra cuatro = compraDeCuatro();
        for (int i = 1; i <= 3; i++) {
            aprobarDe(cuatro, i);
        }

        assertEquals(EstadoDeCompra.ENTREGADA, cuatro.getEstado());
    }

    @Test
    void alAprobarLaCuartaLaCompraPasaAReembolsada() {
        Compra cuatro = compraDeCuatro();
        for (int i = 1; i <= 4; i++) {
            aprobarDe(cuatro, i);
        }

        assertEquals(EstadoDeCompra.REEMBOLSADA, cuatro.getEstado());
    }

    @Test
    void unaSolicitudRechazadaNoCuentaParaReembolsarLaCompra() {
        Compra cuatro = compraDeCuatro();
        for (int i = 1; i <= 3; i++) {
            aprobarDe(cuatro, i);
        }
        solicitudes.buscarPorId("reembolso-4").orElseThrow().rechazar("vendedor-4", ENTREGA.plusHours(10));

        assertEquals(EstadoDeCompra.ENTREGADA, cuatro.getEstado());
    }

    @Test
    void otroVendedorNoPuedeAprobarYNadaCambia() {
        SolicitudReembolso solicitud = solicitarYGuardar();

        assertThrows(ReglaDominioException.class,
                () -> servicio.aprobar(solicitud, compra, "vendedor-2", ENTREGA.plusHours(5)));

        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
    }

    @Test
    void unaSolicitudDeOtraCompraNoSeApruebaContraEstaCompra() {
        SolicitudReembolso ajena = SolicitudReembolso.solicitar("reembolso-9", "compra-9", "pres-1", "vendedor-7",
                "Llego humedo", ENTREGA.plusHours(2));

        assertThrows(ReglaDominioException.class,
                () -> servicio.aprobar(ajena, compra, "vendedor-7", ENTREGA.plusHours(5)));

        assertEquals(EstadoDeReembolso.SOLICITADO, ajena.getEstado());
        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
    }
}
