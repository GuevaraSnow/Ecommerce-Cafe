package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** reconstruir revive una Presentación guardada tal como quedó, sin las validaciones de publicar. */
class ReconstruirPresentacionTest {

    private static final Galeria GALERIA = new Galeria(List.of(
            new ImagenDePresentacion("http://img/1.png", true), new ImagenDePresentacion("http://img/2.png", false)));
    private static final Descuento DESCUENTO = new Descuento(20, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));

    @Test
    void unaTrazableReconstruidaConservaTodosSusCampos() {
        PresentacionTrazable presentacion = PresentacionTrazable.reconstruir("pres-1", "vendedor-1", "Café Tostado",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(20, "kg"), GALERIA,
                EstadoDePublicacion.INACTIVA, false, DESCUENTO, new OrigenDePresentacion(null, "transformacion-1"),
                PerfilTueste.MEDIO, new FechaDeTueste(LocalDate.now().minusDays(3)),
                new NotaDeCata(List.of("Chocolate", "Panela")));

        assertEquals("pres-1", presentacion.getId());
        assertEquals("vendedor-1", presentacion.getVendedorId());
        assertEquals("Café Tostado", presentacion.getTitulo());
        assertEquals(TipoDePresentacion.CAFE_TOSTADO, presentacion.getTipoPresentacion());
        assertEquals(new Precio(32000, "COP"), presentacion.getPrecio());
        assertEquals(new Cantidad(20, "kg"), presentacion.getCantidadDisponible());
        assertEquals(GALERIA, presentacion.getGaleria());
        assertEquals(EstadoDePublicacion.INACTIVA, presentacion.getEstado());
        assertFalse(presentacion.isEliminada());
        assertEquals(DESCUENTO, presentacion.getDescuento());
        assertEquals(new OrigenDePresentacion(null, "transformacion-1"), presentacion.getOrigen());
        assertEquals(PerfilTueste.MEDIO, presentacion.getPerfilDeTueste());
        assertEquals(new NotaDeCata(List.of("Chocolate", "Panela")), presentacion.getNotaDeCata());
    }

    @Test
    void reconstruirNoValidaLaFrescuraYUnaTostadaVencidaSeReportaVencida() {
        PresentacionTrazable presentacion = PresentacionTrazable.reconstruir("pres-1", "vendedor-1", "Café Tostado",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(20, "kg"), GALERIA,
                EstadoDePublicacion.ACTIVA, false, null, new OrigenDePresentacion(null, "transformacion-1"),
                PerfilTueste.MEDIO, new FechaDeTueste(LocalDate.now().minusDays(60)), null);

        assertEquals(EstadoDePublicacion.VENCIDA, presentacion.getEstado());
        assertFalse(presentacion.estaDisponibleParaVenta());
    }

    @Test
    void reconstruirConservaLaBajaLogicaYNoLaDeshace() {
        PresentacionTrazable presentacion = PresentacionTrazable.reconstruir("pres-1", "vendedor-1", "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), GALERIA,
                EstadoDePublicacion.ACTIVA, true, null, new OrigenDePresentacion("LOT-2026-001", null),
                null, null, null);

        assertTrue(presentacion.isEliminada());
        assertNull(presentacion.getDescuento());
        assertNull(presentacion.getNotaDeCata());
        assertFalse(presentacion.estaDisponibleParaVenta());
    }

    @Test
    void unArticuloReconstruidoConservaTodosSusCampos() {
        ArticuloDeMerchandising articulo = ArticuloDeMerchandising.reconstruir("pres-2", "vendedor-2", "Manilla",
                TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(0, "unidad"), GALERIA,
                EstadoDePublicacion.AGOTADA, false, DESCUENTO, "semilla de café", "Manilla artesanal");

        assertEquals("pres-2", articulo.getId());
        assertEquals(EstadoDePublicacion.AGOTADA, articulo.getEstado());
        assertEquals(new Cantidad(0, "unidad"), articulo.getCantidadDisponible());
        assertEquals(DESCUENTO, articulo.getDescuento());
        assertEquals("semilla de café", articulo.getMaterial());
        assertEquals("Manilla artesanal", articulo.getDescripcion());
    }

    @Test
    void unaPresentacionReconstruidaSigueObedeciendoLasReglasDelDominio() {
        ArticuloDeMerchandising articulo = ArticuloDeMerchandising.reconstruir("pres-2", "vendedor-2", "Manilla",
                TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(10, "unidad"), GALERIA,
                EstadoDePublicacion.INACTIVA, false, null, "semilla de café", "Manilla artesanal");

        articulo.activar();

        assertEquals(EstadoDePublicacion.ACTIVA, articulo.getEstado());
    }
}
