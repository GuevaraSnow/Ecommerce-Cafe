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
}
