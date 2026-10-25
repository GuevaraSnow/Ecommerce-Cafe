package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.TransformacionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioDeReposicionTest {

    private final LoteRepositorioMemoria lotes = new LoteRepositorioMemoria();
    private final TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
    private final ServicioDeReposicion servicio = new ServicioDeReposicion(lotes, transformaciones);

    private Galeria galeria() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/cafe.png", true)));
    }

    private void registrarLote(String codigo, double kg) {
        lotes.guardar(Lote.registrar(new CodigoDeLote(codigo), new Cantidad(kg, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL), new VariedadDeCafe("Caturra")));
    }

    private Presentacion cafeVerdeDelLote(String codigo, double kg) {
        return Presentacion.publicar("pres-1", "vendedor-1", codigo, null, "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(kg, "kg"), galeria(), null,
                Rol.CAFICULTOR, null, null, null);
    }

    private Presentacion merchandising() {
        return Presentacion.publicar("pres-2", "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"), galeria(), null,
                Rol.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    private Cantidad disponibleEnLote(String codigo) {
        return lotes.buscarPorCodigo(new CodigoDeLote(codigo)).get().getCantidadDisponible();
    }

    @Test
    void reponerCafeSinTostarDescuentaDelLoteYSumaALaPresentacion() {
        registrarLote("LOT-2026-001", 100);
        Presentacion presentacion = cafeVerdeDelLote("LOT-2026-001", 30);

        servicio.reponer(presentacion, new Cantidad(20, "kg"));

        assertEquals(new Cantidad(50, "kg"), presentacion.getCantidadDisponible());
        assertEquals(new Cantidad(80, "kg"), disponibleEnLote("LOT-2026-001"));
    }

    @Test
    void noSePuedeReponerMasDeLoQueTieneElLoteYNadaCambia() {
        registrarLote("LOT-2026-002", 10);
        Presentacion presentacion = cafeVerdeDelLote("LOT-2026-002", 30);

        assertThrows(ReglaDominioException.class, () -> servicio.reponer(presentacion, new Cantidad(20, "kg")));

        assertEquals(new Cantidad(30, "kg"), presentacion.getCantidadDisponible());
        assertEquals(new Cantidad(10, "kg"), disponibleEnLote("LOT-2026-002"));
    }

    @Test
    void reponerCafeTostadoDescuentaDeLaTransformacion() {
        transformaciones.guardar(Transformacion.registrar("t-1", "LOT-2026-001", new Cantidad(50, "kg"),
                new PorcentajeDeMerma(0.18), "tostador-1", LocalDate.now(), PerfilTueste.MEDIO)); // resultante 41 kg
        Presentacion tostado = Presentacion.publicar("pres-3", "vendedor-1", null, "t-1", "Café Medio",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(10, "kg"), galeria(),
                PerfilTueste.MEDIO, Rol.TOSTADOR, new FechaDeTueste(LocalDate.now()), null, null);

        servicio.reponer(tostado, new Cantidad(15, "kg"));

        assertEquals(new Cantidad(25, "kg"), tostado.getCantidadDisponible());
        assertEquals(26, transformaciones.buscarPorId("t-1").get().getCantidadResultanteDisponible().valor(),
                0.0001);
    }

    @Test
    void elMerchandisingSeReponeDirectamenteSinOrigen() {
        Presentacion manilla = merchandising();

        servicio.reponer(manilla, new Cantidad(5, "unidad"));

        assertEquals(new Cantidad(25, "unidad"), manilla.getCantidadDisponible());
    }

    @Test
    void siLaPresentacionFueDadaDeBajaNoSeDescuentaNadaDelOrigen() {
        registrarLote("LOT-2026-003", 100);
        Presentacion presentacion = cafeVerdeDelLote("LOT-2026-003", 30);
        presentacion.darDeBaja();

        assertThrows(ReglaDominioException.class, () -> servicio.reponer(presentacion, new Cantidad(20, "kg")));

        assertEquals(new Cantidad(100, "kg"), disponibleEnLote("LOT-2026-003"));
    }

    @Test
    void siLaUnidadNoCoincideNoSeDescuentaNadaDelOrigen() {
        registrarLote("LOT-2026-004", 100);
        Presentacion presentacion = cafeVerdeDelLote("LOT-2026-004", 30);

        assertThrows(ReglaDominioException.class, () -> servicio.reponer(presentacion, new Cantidad(5000, "g")));

        assertEquals(new Cantidad(100, "kg"), disponibleEnLote("LOT-2026-004"));
        assertEquals(new Cantidad(30, "kg"), presentacion.getCantidadDisponible());
    }

    @Test
    void noSePuedeReponerUnaCantidadEnCero() {
        Presentacion manilla = merchandising();

        assertThrows(ReglaDominioException.class, () -> servicio.reponer(manilla, new Cantidad(0, "unidad")));
    }
}