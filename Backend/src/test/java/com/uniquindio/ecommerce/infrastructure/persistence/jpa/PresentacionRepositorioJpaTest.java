package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba el repositorio de Presentación contra H2 de verdad y con los datos de data-presentacion.sql.
 * Pide el puerto del dominio, no el adaptador. Cada prueba deshace lo que guarda (@Transactional).
 *
 * Datos disponibles para la venta, ordenados por título:
 * pres-cafe-1 (Cafe tostado Huila), pres-cafe-3 (Cafe tostado Narino), pres-cafe-2 (Cafe verde Quindio)
 * y pres-taza-1 (Taza de ceramica). Las demás filas (vencida, pausada, agotada, eliminada) no deben aparecer.
 */
@SpringBootTest
@Transactional
class PresentacionRepositorioJpaTest {

    private static final FiltroDePresentaciones SIN_FILTROS = new FiltroDePresentaciones(null, null, null, null, null);

    @Autowired
    private PresentacionRepositorio repositorio;

    @Autowired
    private EntityManager entityManager;

    private List<String> ids(List<Presentacion> presentaciones) {
        return presentaciones.stream().map(Presentacion::getId).toList();
    }

    /** Fuerza que lo guardado llegue a la base y que la lectura siguiente salga de ella, no de la memoria de JPA. */
    private void vaciarLaMemoriaDeJpa() {
        entityManager.flush();
        entityManager.clear();
    }

    // ----- guardar y recuperar -----

    @Test
    void guardarYRecuperarUnArticuloDevuelveLosMismosDatosConSuGaleriaYSuDescuento() {
        Galeria galeria = new Galeria(List.of(new ImagenDePresentacion("http://img/a.png", false),
                new ImagenDePresentacion("http://img/b.png", true), new ImagenDePresentacion("http://img/c.png", false)));
        Presentacion articulo = Presentacion.publicar("pres-nueva", "usr-derivados", null, null, "Manilla",
                TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"), galeria, null,
                Rol.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
        articulo.programarDescuento(new Descuento(25, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)));

        repositorio.guardar(articulo);
        vaciarLaMemoriaDeJpa();
        ArticuloDeMerchandising recuperado = assertInstanceOf(ArticuloDeMerchandising.class,
                repositorio.buscarPorId("pres-nueva").orElseThrow());

        assertEquals("usr-derivados", recuperado.getVendedorId());
        assertEquals("Manilla", recuperado.getTitulo());
        assertEquals(new Precio(15000, "COP"), recuperado.getPrecio());
        assertEquals(new Cantidad(20, "unidad"), recuperado.getCantidadDisponible());
        assertEquals(galeria, recuperado.getGaleria());
        assertEquals(new Descuento(25, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)), recuperado.getDescuento());
        assertEquals(EstadoDePublicacion.ACTIVA, recuperado.getEstado());
        assertEquals("semilla de café", recuperado.getMaterial());
        assertEquals("Manilla artesanal", recuperado.getDescripcion());
    }

    @Test
    void guardarYRecuperarUnaTrazableConservaOrigenPerfilFechaYNotaDeCata() {
        PresentacionTrazable tostada = (PresentacionTrazable) Presentacion.publicar("pres-tostada", "usr-tostador", null,
                "transformacion-9", "Café tostado", TipoDePresentacion.CAFE_TOSTADO, new Precio(30000, "COP"),
                new Cantidad(12, "kg"), new Galeria(List.of(new ImagenDePresentacion("http://img/t.png", true))),
                PerfilTueste.OSCURO, Rol.TOSTADOR, new FechaDeTueste(LocalDate.now().minusDays(2)), null, null);
        tostada.registrarNotaCata(new NotaDeCata(List.of("Chocolate", "Panela")));

        repositorio.guardar(tostada);
        vaciarLaMemoriaDeJpa();
        PresentacionTrazable recuperada = assertInstanceOf(PresentacionTrazable.class,
                repositorio.buscarPorId("pres-tostada").orElseThrow());

        assertEquals(new OrigenDePresentacion(null, "transformacion-9"), recuperada.getOrigen());
        assertEquals(PerfilTueste.OSCURO, recuperada.getPerfilDeTueste());
        assertEquals(new FechaDeTueste(LocalDate.now().minusDays(2)), recuperada.getFechaTueste());
        assertEquals(new NotaDeCata(List.of("Chocolate", "Panela")), recuperada.getNotaDeCata());
    }

    @Test
    void guardarDeNuevoUnaPresentacionModificadaActualizaLaFilaYNoLaDuplica() {
        Presentacion cafe = repositorio.buscarPorId("pres-cafe-2").orElseThrow();
        cafe.cambiarPrecio(new Precio(35000, "COP"));
        cafe.descontarCantidad(new Cantidad(50, "kg"));

        repositorio.guardar(cafe);
        vaciarLaMemoriaDeJpa();
        Presentacion recuperada = repositorio.buscarPorId("pres-cafe-2").orElseThrow();

        assertEquals(new Precio(35000, "COP"), recuperada.getPrecio());
        assertEquals(new Cantidad(150, "kg"), recuperada.getCantidadDisponible());
        assertEquals(4, repositorio.buscarDisponibles(SIN_FILTROS, 0, 10).totalElementos());
    }

    @Test
    void buscarUnIdQueNoExisteDevuelveVacio() {
        assertTrue(repositorio.buscarPorId("no-existe").isEmpty());
    }

    // ----- datos del data-presentacion.sql -----

    @Test
    void losDatosDeEjemploSeCargaronConSuGaleriaNotaYDescuento() {
        PresentacionTrazable tostada = assertInstanceOf(PresentacionTrazable.class,
                repositorio.buscarPorId("pres-cafe-1").orElseThrow());
        PresentacionTrazable conDescuento = assertInstanceOf(PresentacionTrazable.class,
                repositorio.buscarPorId("pres-cafe-3").orElseThrow());

        assertEquals(List.of(new ImagenDePresentacion("http://img/cafe-1-principal.png", true),
                new ImagenDePresentacion("http://img/cafe-1-detalle.png", false)), tostada.getGaleria().imagenes());
        assertEquals(new NotaDeCata(List.of("Chocolate", "Panela")), tostada.getNotaDeCata());
        assertNull(tostada.getDescuento());
        assertEquals(20, conDescuento.getDescuento().porcentaje());
        assertEquals(new Precio(36000, "COP"), conDescuento.precioVigente(LocalDate.now()));
    }

    // ----- búsqueda: solo lo disponible, ordenado y paginado -----

    @Test
    void sinFiltrosListaSoloLasCuatroDisponiblesOrdenadasPorTitulo() {
        Pagina<Presentacion> pagina = repositorio.buscarDisponibles(SIN_FILTROS, 0, 10);

        assertEquals(List.of("pres-cafe-1", "pres-cafe-3", "pres-cafe-2", "pres-taza-1"), ids(pagina.contenido()));
        assertEquals(4, pagina.totalElementos());
        assertEquals(1, pagina.totalPaginas());
    }

    @Test
    void laPaginacionDevuelveElTamanoPedidoYLosTotalesCorrectos() {
        Pagina<Presentacion> primera = repositorio.buscarDisponibles(SIN_FILTROS, 0, 3);
        Pagina<Presentacion> segunda = repositorio.buscarDisponibles(SIN_FILTROS, 1, 3);

        assertEquals(List.of("pres-cafe-1", "pres-cafe-3", "pres-cafe-2"), ids(primera.contenido()));
        assertEquals(List.of("pres-taza-1"), ids(segunda.contenido()));
        assertEquals(4, primera.totalElementos());
        assertEquals(2, primera.totalPaginas());
    }

    @Test
    void unaPaginaMasAllaDelFinalVieneVaciaPeroConElTotal() {
        Pagina<Presentacion> pagina = repositorio.buscarDisponibles(SIN_FILTROS, 5, 3);

        assertTrue(pagina.contenido().isEmpty());
        assertEquals(4, pagina.totalElementos());
    }

    @Test
    void noApareceNiLaVencidaNiLaPausadaNiLaAgotadaNiLaEliminada() {
        List<String> disponibles = ids(repositorio.buscarDisponibles(SIN_FILTROS, 0, 20).contenido());

        assertFalse(disponibles.contains("pres-vencida"));
        assertFalse(disponibles.contains("pres-pausada"));
        assertFalse(disponibles.contains("pres-agotada"));
        assertFalse(disponibles.contains("pres-eliminada"));
    }

    @Test
    void lasNoDisponiblesSiSePuedenConsultarPorIdConSuEstado() {
        assertEquals(EstadoDePublicacion.VENCIDA, repositorio.buscarPorId("pres-vencida").orElseThrow().getEstado());
        assertEquals(EstadoDePublicacion.INACTIVA, repositorio.buscarPorId("pres-pausada").orElseThrow().getEstado());
        assertEquals(EstadoDePublicacion.AGOTADA, repositorio.buscarPorId("pres-agotada").orElseThrow().getEstado());
        assertTrue(repositorio.buscarPorId("pres-eliminada").orElseThrow().isEliminada());
    }

    @Test
    void unaPresentacionDadaDeBajaLogicamenteDejaDeAparecerEnLasBusquedas() {
        Presentacion taza = repositorio.buscarPorId("pres-taza-1").orElseThrow();
        taza.darDeBaja();

        repositorio.guardar(taza);
        vaciarLaMemoriaDeJpa();

        assertFalse(ids(repositorio.buscarDisponibles(SIN_FILTROS, 0, 10).contenido()).contains("pres-taza-1"));
        assertEquals(3, repositorio.buscarDisponibles(SIN_FILTROS, 0, 10).totalElementos());
        assertTrue(repositorio.buscarPorId("pres-taza-1").orElseThrow().isEliminada());
    }

    // ----- filtros -----

    @Test
    void filtraPorTipoYPorVendedor() {
        assertEquals(List.of("pres-cafe-1", "pres-cafe-3"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, TipoDePresentacion.CAFE_TOSTADO, null, null, null), 0, 10).contenido()));
        assertEquals(List.of("pres-cafe-1", "pres-cafe-3"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, "usr-tostador", null, null), 0, 10).contenido()));
    }

    @Test
    void filtraPorTituloSinDistinguirMayusculas() {
        assertEquals(List.of("pres-taza-1"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones("TAZA", null, null, null, null), 0, 10).contenido()));
    }

    @Test
    void filtraPorRangoDePrecioIncluyendoLosExtremos() {
        Pagina<Presentacion> pagina = repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, 32000.0, 45000.0), 0, 10);

        assertEquals(List.of("pres-cafe-3", "pres-cafe-2"), ids(pagina.contenido()));
    }

    @Test
    void combinaLosFiltros() {
        Pagina<Presentacion> pagina = repositorio.buscarDisponibles(
                new FiltroDePresentaciones("cafe", TipoDePresentacion.CAFE_TOSTADO, "usr-tostador", null, 30000.0), 0, 10);

        assertEquals(List.of("pres-cafe-1"), ids(pagina.contenido()));
        assertEquals(1, pagina.totalElementos());
    }

    // ----- sugerencias -----

    @Test
    void sugiereLasDisponiblesQueContienenElTextoOrdenadasYLimitadas() {
        assertEquals(List.of("pres-cafe-1", "pres-cafe-3", "pres-cafe-2"),
                ids(repositorio.sugerirDisponiblesPorTitulo("caf", 10)));
        assertEquals(List.of("pres-cafe-1", "pres-cafe-3"), ids(repositorio.sugerirDisponiblesPorTitulo("caf", 2)));
        assertEquals(List.of("pres-taza-1"), ids(repositorio.sugerirDisponiblesPorTitulo("TAZ", 10)));
        assertTrue(repositorio.sugerirDisponiblesPorTitulo("xyz", 10).isEmpty());
    }

    @Test
    void lasSugerenciasTampocoIncluyenLasNoDisponibles() {
        assertTrue(repositorio.sugerirDisponiblesPorTitulo("vencido", 10).isEmpty());
        assertTrue(repositorio.sugerirDisponiblesPorTitulo("llavero", 10).isEmpty());
        assertTrue(repositorio.sugerirDisponiblesPorTitulo("gorra", 10).isEmpty());
    }
}
