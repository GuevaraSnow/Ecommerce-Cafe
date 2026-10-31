package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Las consultas del catálogo: solo lo disponible para la venta, con filtros, orden por título y paginación. */
class PresentacionRepositorioMemoriaTest {

    private static final FiltroDePresentaciones SIN_FILTROS = new FiltroDePresentaciones(null, null, null, null, null);

    private final ResenaRepositorioMemoria resenas = new ResenaRepositorioMemoria();
    private final PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria(resenas);

    private Presentacion merchandising(String id, String vendedorId, String titulo, double precio) {
        return Presentacion.publicar(id, vendedorId, null, null, titulo, TipoDePresentacion.MERCHANDISING,
                new Precio(precio, "COP"), new Cantidad(20, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/1.png", true))), null,
                Rol.VENDEDOR_DERIVADOS, null, "semilla", "descripcion");
    }

    private Presentacion cafeVerde(String id, String titulo, double precio) {
        return Presentacion.publicar(id, "vendedor-2", "LOT-2026-001", null, titulo, TipoDePresentacion.CAFE_VERDE,
                new Precio(precio, "COP"), new Cantidad(10, "kg"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/1.png", true))), null,
                Rol.CAFICULTOR, null, null, null);
    }

    private Resena resena(String id, String presentacionId, int calificacion) {
        return Resena.dejar(id, "comprador-" + id, presentacionId, "vendedor-1", new Calificacion(calificacion),
                new TextoDeResena("Opinion"), LocalDateTime.of(2026, 10, 20, 9, 0));
    }

    private List<String> ids(Pagina<Presentacion> pagina) {
        return pagina.contenido().stream().map(Presentacion::getId).toList();
    }

    @BeforeEach
    void catalogo() {
        repositorio.guardar(merchandising("p-manilla", "vendedor-1", "Manilla", 15000));
        repositorio.guardar(merchandising("p-taza", "vendedor-1", "Taza", 30000));
        repositorio.guardar(cafeVerde("p-cafe", "Café de la montaña", 28000));
    }

    @Test
    void buscarNoEliminadaNoDevuelveUnaPresentacionDadaDeBaja() {
        repositorio.buscarPorId("p-taza").orElseThrow().darDeBaja();

        assertTrue(repositorio.buscarNoEliminadaPorId("p-manilla").isPresent());
        assertTrue(repositorio.buscarNoEliminadaPorId("p-taza").isEmpty());
        assertTrue(repositorio.buscarPorId("p-taza").isPresent());
    }

    @Test
    void sinFiltrosListaTodoOrdenadoPorTitulo() {
        Pagina<Presentacion> pagina = repositorio.buscarDisponibles(SIN_FILTROS, 0, 10);

        assertEquals(List.of("p-cafe", "p-manilla", "p-taza"), ids(pagina));
        assertEquals(3, pagina.totalElementos());
        assertEquals(1, pagina.totalPaginas());
    }

    @Test
    void filtraPorTituloSinDistinguirMayusculas() {
        FiltroDePresentaciones filtro = new FiltroDePresentaciones("MONTA", null, null, null, null);

        assertEquals(List.of("p-cafe"), ids(repositorio.buscarDisponibles(filtro, 0, 10)));
    }

    @Test
    void filtraPorTipoYPorVendedor() {
        assertEquals(List.of("p-cafe"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, TipoDePresentacion.CAFE_VERDE, null, null, null), 0, 10)));
        assertEquals(List.of("p-manilla", "p-taza"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, "vendedor-1", null, null), 0, 10)));
    }

    @Test
    void filtraPorRangoDePrecioIncluyendoLosExtremos() {
        FiltroDePresentaciones filtro = new FiltroDePresentaciones(null, null, null, 15000.0, 28000.0);

        assertEquals(List.of("p-cafe", "p-manilla"), ids(repositorio.buscarDisponibles(filtro, 0, 10)));
    }

    @Test
    void filtraPorCalificacionMinimaConElPromedioDeLasResenas() {
        resenas.guardar(resena("r1", "p-manilla", 5));
        resenas.guardar(resena("r2", "p-manilla", 3));   // promedio 4
        resenas.guardar(resena("r3", "p-taza", 2));      // promedio 2; p-cafe no tiene reseñas

        assertEquals(List.of("p-manilla"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 4.0), 0, 10)));
        assertEquals(List.of("p-manilla", "p-taza"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 2.0), 0, 10)));
        assertTrue(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 4.5), 0, 10).contenido().isEmpty());
    }

    @Test
    void conUnaCalificacionMinimaNoApareceUnaPresentacionSinResenas() {
        assertTrue(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 1.0), 0, 10).contenido().isEmpty());
    }

    @Test
    void combinaLosFiltros() {
        FiltroDePresentaciones filtro = new FiltroDePresentaciones(null, TipoDePresentacion.MERCHANDISING,
                "vendedor-1", null, 20000.0);

        assertEquals(List.of("p-manilla"), ids(repositorio.buscarDisponibles(filtro, 0, 10)));
    }

    @Test
    void paginaLosResultadosYInformaLosTotales() {
        Pagina<Presentacion> primera = repositorio.buscarDisponibles(SIN_FILTROS, 0, 2);
        Pagina<Presentacion> segunda = repositorio.buscarDisponibles(SIN_FILTROS, 1, 2);
        Pagina<Presentacion> fuera = repositorio.buscarDisponibles(SIN_FILTROS, 5, 2);

        assertEquals(List.of("p-cafe", "p-manilla"), ids(primera));
        assertEquals(List.of("p-taza"), ids(segunda));
        assertTrue(fuera.contenido().isEmpty());
        assertEquals(3, primera.totalElementos());
        assertEquals(2, primera.totalPaginas());
    }

    @Test
    void noListaLasPresentacionesPausadasAgotadasNiDadasDeBaja() {
        repositorio.buscarPorId("p-manilla").orElseThrow().desactivar();
        repositorio.buscarPorId("p-taza").orElseThrow().darDeBaja();
        repositorio.buscarPorId("p-cafe").orElseThrow().descontarCantidad(new Cantidad(10, "kg"));

        assertTrue(repositorio.buscarDisponibles(SIN_FILTROS, 0, 10).contenido().isEmpty());
        assertTrue(repositorio.sugerirDisponiblesPorTitulo("a", 10).isEmpty());
    }

    @Test
    void sugiereLasQueContienenElTextoOrdenadasYLimitadas() {
        assertEquals(List.of("p-cafe", "p-manilla", "p-taza"),
                repositorio.sugerirDisponiblesPorTitulo("a", 10).stream().map(Presentacion::getId).toList());
        assertEquals(List.of("p-cafe", "p-manilla"),
                repositorio.sugerirDisponiblesPorTitulo("a", 2).stream().map(Presentacion::getId).toList());
        assertEquals(List.of("p-taza"),
                repositorio.sugerirDisponiblesPorTitulo("TAZ", 10).stream().map(Presentacion::getId).toList());
        assertTrue(repositorio.sugerirDisponiblesPorTitulo("xyz", 10).isEmpty());
    }
}
