package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las reglas propias de PresentacionTrazable: Origen exclusivo
 * (regla B), frescura (regla D), Perfil de Tueste según rol (regla A) y la
 * unidad de Cantidad. Llama directamente a PresentacionTrazable.publicar()
 * (visibilidad de paquete) en vez de pasar por Presentacion.publicar().
 */
class PresentacionTrazableTest {

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/cafe.png", true)));
    }

    @Test
    void cafeVerdeDesdeUnLoteEsValido() {
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-1", "vendedor-1", "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeriaValida(),
                "lote-1", null, null, null, RolVendedor.CAFICULTOR);

        assertEquals("lote-1", presentacion.getOrigen().loteId());
        assertNull(presentacion.getOrigen().transformacionId());
    }

    @Test
    void cafeVerdeDesdeUnaTransformacionFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-2", "vendedor-1",
                "Café Verde", TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"),
                galeriaValida(), null, "transformacion-1", null, null, RolVendedor.CAFICULTOR));
    }

    @Test
    void sinLoteNiTransformacionFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-3", "vendedor-1",
                "Café Verde", TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"),
                galeriaValida(), null, null, null, null, RolVendedor.CAFICULTOR));
    }

    @Test
    void conLoteYTransformacionALaVezFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-4", "vendedor-1",
                "Café Verde", TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"),
                galeriaValida(), "lote-1", "transformacion-1", null, null, RolVendedor.CAFICULTOR));
    }

    @Test
    void cafeTostadoDesdeUnaTransformacionEsValido() {
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-5", "vendedor-1", "Café Tostado",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"), galeriaValida(),
                null, "transformacion-1", PerfilTueste.MEDIO, new FechaDeTueste(LocalDate.now()),
                RolVendedor.TOSTADOR);

        assertEquals("transformacion-1", presentacion.getOrigen().transformacionId());
        assertEquals(PerfilTueste.MEDIO, presentacion.getPerfilDeTueste());
    }

    @Test
    void cafeTostadoDesdeUnLoteFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-6", "vendedor-1",
                "Café Tostado", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"),
                galeriaValida(), "lote-1", null, PerfilTueste.MEDIO, new FechaDeTueste(LocalDate.now()),
                RolVendedor.TOSTADOR));
    }

    @Test
    void cafeTostadoSinFechaDeTuesteFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-7", "vendedor-1",
                "Café Tostado", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"),
                galeriaValida(), null, "transformacion-1", PerfilTueste.MEDIO, null, RolVendedor.TOSTADOR));
    }

    @Test
    void cafeTostadoSinPerfilDeTuesteFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-8", "vendedor-1",
                "Café Tostado", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"),
                galeriaValida(), null, "transformacion-1", null, new FechaDeTueste(LocalDate.now()),
                RolVendedor.TOSTADOR));
    }

    @Test
    void cafeTostadoConMasDe30DiasDeTuesteFalla() {
        FechaDeTueste fechaVencida = new FechaDeTueste(LocalDate.now().minusDays(31));

        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-9", "vendedor-1",
                "Café Tostado", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"),
                galeriaValida(), null, "transformacion-1", PerfilTueste.MEDIO, fechaVencida, RolVendedor.TOSTADOR));
    }

    @Test
    void cafeVerdeConFechaDeTuesteFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-10", "vendedor-1",
                "Café Verde", TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"),
                galeriaValida(), "lote-1", null, null, new FechaDeTueste(LocalDate.now()), RolVendedor.CAFICULTOR));
    }

    @Test
    void unCaficultorSoloPuedePublicarCafeTostadoTradicional() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-11", "vendedor-1",
                "Café Tostado", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"),
                galeriaValida(), null, "transformacion-1", PerfilTueste.OSCURO, new FechaDeTueste(LocalDate.now()),
                RolVendedor.CAFICULTOR));
    }

    @Test
    void unCaficultorPuedePublicarCafeTostadoTradicional() {
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-12", "vendedor-1", "Café Tostado",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"), galeriaValida(),
                null, "transformacion-1", PerfilTueste.TRADICIONAL, new FechaDeTueste(LocalDate.now()),
                RolVendedor.CAFICULTOR);

        assertEquals(PerfilTueste.TRADICIONAL, presentacion.getPerfilDeTueste());
    }

    @Test
    void sinRolDeVendedorFalla() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-13", "vendedor-1",
                "Café Verde", TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"),
                galeriaValida(), "lote-1", null, null, null, null));
    }

    @Test
    void noSePuedeContarEnUnidadComoElMerchandising() {
        assertThrows(ReglaDominioException.class, () -> PresentacionTrazable.publicar("pres-14", "vendedor-1",
                "Café Verde", TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "unidad"),
                galeriaValida(), "lote-1", null, null, null, RolVendedor.CAFICULTOR));
    }

    @Test
    void estaFrescaEsVerdaderaCuandoNoHayFechaDeTueste() {
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-15", "vendedor-1", "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeriaValida(),
                "lote-1", null, null, null, RolVendedor.CAFICULTOR);

        assertTrue(presentacion.estaFresca());
    }

    @Test
    void estaFrescaEsVerdaderaEnElLimiteDe30Dias() {
        FechaDeTueste fechaLimite = new FechaDeTueste(LocalDate.now().minusDays(30));
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-16", "vendedor-1", "Café Tostado",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(50, "kg"), galeriaValida(),
                null, "transformacion-1", PerfilTueste.MEDIO, fechaLimite, RolVendedor.TOSTADOR);

        assertTrue(presentacion.estaFresca());
    }

    @Test
    void verificarVencimientoNoCambiaElEstadoSiEstaFresca() {
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-17", "vendedor-1", "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeriaValida(),
                "lote-1", null, null, null, RolVendedor.CAFICULTOR);

        presentacion.verificarVencimiento();

        assertEquals(EstadoDePublicacion.ACTIVA, presentacion.getEstado());
    }

    @Test
    void registrarNotaDeCataNulaLanzaExcepcion() {
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-18", "vendedor-1", "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeriaValida(),
                "lote-1", null, null, null, RolVendedor.CAFICULTOR);

        assertThrows(ReglaDominioException.class, () -> presentacion.registrarNotaCata(null));
    }

    @Test
    void registrarNotaDeCataValidaLaAsigna() {
        PresentacionTrazable presentacion = PresentacionTrazable.publicar("pres-19", "vendedor-1", "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeriaValida(),
                "lote-1", null, null, null, RolVendedor.CAFICULTOR);
        NotaDeCata nota = new NotaDeCata(List.of("Chocolate", "Panela"));

        presentacion.registrarNotaCata(nota);

        assertEquals(nota, presentacion.getNotaDeCata());
    }
}
