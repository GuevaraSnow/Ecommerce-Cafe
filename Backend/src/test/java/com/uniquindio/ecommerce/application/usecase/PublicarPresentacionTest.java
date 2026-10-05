package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.TransformacionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PublicarPresentacionTest {

    private final PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
    private final LoteRepositorioMemoria lotes = new LoteRepositorioMemoria();
    private final TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
    private final PublicarPresentacion publicar = new PublicarPresentacion(presentaciones, lotes, transformaciones);

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/cafe.png", true)));
    }

    private void registrarLote(String codigo, double cantidadKg) {
        lotes.guardar(Lote.registrar(new CodigoDeLote(codigo), new Cantidad(cantidadKg, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL), new VariedadDeCafe("Caturra")));
    }

    private Cantidad disponibleEnLote(String codigo) {
        return lotes.buscarPorCodigo(new CodigoDeLote(codigo)).get().getCantidadDisponible();
    }

    private void registrarTransformacion(String id, double cantidadEntradaKg) {
        transformaciones.guardar(Transformacion.registrar(id, List.of("LOT-2026-001"),
                new Cantidad(cantidadEntradaKg, "kg"), new PorcentajeDeMerma(0.18), "tostador-1", LocalDate.now(),
                PerfilTueste.MEDIO));
    }

    private Presentacion publicarCafeVerde(String id, String loteId, double cantidadKg) {
        return publicar.ejecutar(id, "vendedor-1", loteId, null, "Café Verde", TipoDePresentacion.CAFE_VERDE,
                new Precio(28000, "COP"), new Cantidad(cantidadKg, "kg"), galeriaValida(), null,
                RolVendedor.CAFICULTOR, null);
    }

    @Test
    void publicaCafeTostadoYDescuentaLaCantidadResultanteDisponible() {
        registrarTransformacion("transformacion-1", 50); // resultante = 41 kg (18% merma)

        Presentacion presentacion = publicar.ejecutar("pres-1", "vendedor-1", null, "transformacion-1",
                "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(20, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now()));

        assertTrue(presentacion instanceof PresentacionTrazable);
        assertEquals(21, transformaciones.buscarPorId("transformacion-1").get()
                .getCantidadResultanteDisponible().valor(), 0.0001);
        assertTrue(presentaciones.buscarPorId("pres-1").isPresent());
    }

    @Test
    void noSePuedePublicarMasCantidadDeLaResultanteDisponible() {
        registrarTransformacion("transformacion-2", 50); // resultante = 41 kg

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-2", "vendedor-1", null,
                "transformacion-2", "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(50, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now())));

        assertTrue(presentaciones.buscarPorId("pres-2").isEmpty());
    }

    @Test
    void publicarDosVecesDeLaMismaTransformacionAcumulaElDescuento() {
        registrarTransformacion("transformacion-3", 50); // resultante = 41 kg

        publicar.ejecutar("pres-3a", "vendedor-1", null, "transformacion-3", "Café Tostado Medio",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(30, "kg"), galeriaValida(),
                PerfilTueste.MEDIO, RolVendedor.TOSTADOR, new FechaDeTueste(LocalDate.now()));

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-3b", "vendedor-1", null,
                "transformacion-3", "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(15, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now())));
    }

    @Test
    void publicaCafeVerdeDesdeUnLoteYLeDescuentaLaCantidadPublicada() {
        registrarLote("LOT-2026-001", 200);

        Presentacion presentacion = publicarCafeVerde("pres-4", "LOT-2026-001", 30);

        assertTrue(presentacion instanceof PresentacionTrazable);
        assertTrue(presentaciones.buscarPorId("pres-4").isPresent());
        assertEquals(new Cantidad(170, "kg"), disponibleEnLote("LOT-2026-001"));
    }

    @Test
    void noSePuedePublicarCafeVerdeDesdeUnLoteQueNoExiste() {
        assertThrows(ReglaDominioException.class, () -> publicarCafeVerde("pres-5", "LOT-2026-999", 30));

        assertTrue(presentaciones.buscarPorId("pres-5").isEmpty());
    }

    @Test
    void noSePuedePublicarMasCafeVerdeDelQueTieneElLoteYNadaCambia() {
        registrarLote("LOT-2026-002", 50);

        assertThrows(ReglaDominioException.class, () -> publicarCafeVerde("pres-6", "LOT-2026-002", 80));

        assertEquals(new Cantidad(50, "kg"), disponibleEnLote("LOT-2026-002"));
        assertTrue(presentaciones.buscarPorId("pres-6").isEmpty());
    }

    @Test
    void publicarDosVecesDelMismoLoteAcumulaElDescuento() {
        registrarLote("LOT-2026-003", 50);
        publicarCafeVerde("pres-7a", "LOT-2026-003", 30);

        assertThrows(ReglaDominioException.class, () -> publicarCafeVerde("pres-7b", "LOT-2026-003", 30));

        assertEquals(new Cantidad(20, "kg"), disponibleEnLote("LOT-2026-003"));
    }

    @Test
    void noSePuedePublicarDesdeUnLoteEliminado() {
        registrarLote("LOT-2026-004", 100);
        lotes.buscarPorCodigo(new CodigoDeLote("LOT-2026-004")).get().eliminarLogicamente();

        assertThrows(ReglaDominioException.class, () -> publicarCafeVerde("pres-8", "LOT-2026-004", 10));

        assertEquals(new Cantidad(100, "kg"), disponibleEnLote("LOT-2026-004"));
    }

    @Test
    void noSePuedePublicarSiLaTransformacionNoExiste() {
        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-9", "vendedor-1", null,
                "no-existe", "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(10, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now())));

        assertTrue(presentaciones.buscarPorId("pres-9").isEmpty());
    }

    @Test
    void noSePuedeUsarEsteCasoDeUsoParaMerchandising() {
        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-10", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null));
    }
}
