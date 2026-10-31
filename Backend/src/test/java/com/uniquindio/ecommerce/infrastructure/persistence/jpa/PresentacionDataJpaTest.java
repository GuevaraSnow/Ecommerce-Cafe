package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.PresentacionPersistenceMapper;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.ResenaPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lo mismo que PresentacionRepositorioJpaTest pero con @DataJpaTest: levanta solo la capa de
 * persistencia (entidades y repositorios de Spring Data), así que arranca más rápido, y deshace cada
 * prueba sola. No carga nuestro adaptador ni el mapper, por eso se traen con @Import.
 * Por defecto @DataJpaTest cambia la base de datos por una H2 embebida propia; con
 * {@code Replace.NONE} se queda con la de application.properties, con la que sí carga bien
 * data-presentacion.sql.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PresentacionRepositorioJpa.class, PresentacionPersistenceMapper.class,
         ResenaRepositorioJpa.class, ResenaPersistenceMapper.class})
class PresentacionDataJpaTest {

    @Autowired
    private PresentacionRepositorio repositorio;

    @Test
    void cargaLosDatosDeEjemploYLosDevuelveOrdenadosPorTitulo() {
        var pagina = repositorio.buscarDisponibles(new FiltroDePresentaciones(null, null, null, null, null), 0, 10);

        assertEquals(4, pagina.totalElementos());
        assertEquals("pres-cafe-1", pagina.contenido().get(0).getId());
        assertEquals("pres-taza-1", pagina.contenido().get(3).getId());
    }

    private List<String> ids(Pagina<Presentacion> pagina) {
        return pagina.contenido().stream().map(Presentacion::getId).toList();
    }

    @Test
    void buscarNoEliminadaNoDevuelveLaPresentacionDadaDeBajaPeroBuscarPorIdSi() {
        assertTrue(repositorio.buscarNoEliminadaPorId("pres-eliminada").isEmpty());
        assertTrue(repositorio.buscarPorId("pres-eliminada").isPresent());
        assertTrue(repositorio.buscarNoEliminadaPorId("pres-taza-1").isPresent());
    }

    @Test
    void laPresentacionEliminadaDeLosDatosDeEjemploNuncaApareceEnLosResultados() {
        Pagina<Presentacion> todas = repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null), 0, 50);

        assertFalse(ids(todas).contains("pres-eliminada"));
        assertEquals(0, repositorio.buscarDisponibles(
                new FiltroDePresentaciones("Gorra", null, null, null, null), 0, 10).totalElementos());
        assertTrue(repositorio.sugerirDisponiblesPorTitulo("Gorra", 10).isEmpty());
    }

    @Test
    void unaPaginaFueraDeRangoDevuelveContenidoVacioPeroConElTotalReal() {
        Pagina<Presentacion> fuera = repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null), 5, 10);

        assertTrue(fuera.contenido().isEmpty());
        assertEquals(4, fuera.totalElementos());
    }

    @Test
    void filtraPorCalificacionMinimaConElPromedioDeLasResenasDeEjemplo() {
        // pres-cafe-1 tiene una reseña de 5 y pres-cafe-3 una de 2; pres-cafe-2 y pres-taza-1 no tienen reseñas.
        assertEquals(List.of("pres-cafe-1"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 4.0), 0, 10)));
        assertEquals(List.of("pres-cafe-1", "pres-cafe-3"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 1.0), 0, 10)));
    }

    @Test
    void laCalificacionMinimaSeCompararConElPromedioYNoConLaUltimaResena(@Autowired ResenaRepositorio resenas) {
        resenas.guardar(Resena.dejar("resena-nueva", "usr-ana", "pres-cafe-3", "usr-tostador",
                new Calificacion(5), new TextoDeResena("Mejoro mucho"), LocalDateTime.of(2026, 10, 25, 9, 0)));
        // pres-cafe-3 ahora tiene 2 y 5: promedio 3.5

        assertEquals(List.of("pres-cafe-1", "pres-cafe-3"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 3.5), 0, 10)));
        assertEquals(List.of("pres-cafe-1"), ids(repositorio.buscarDisponibles(
                new FiltroDePresentaciones(null, null, null, null, null, 3.6), 0, 10)));
    }

    @Test
    void guardaYRecuperaUnaPresentacionNueva() {
        Presentacion articulo = Presentacion.publicar("pres-dj", "usr-derivados", null, null, "Posavasos",
                TipoDePresentacion.MERCHANDISING, new Precio(9000, "COP"), new Cantidad(30, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/p.png", true))), null,
                Rol.VENDEDOR_DERIVADOS, null, "corcho", "Posavasos de corcho");

        repositorio.guardar(articulo);

        assertTrue(repositorio.buscarPorId("pres-dj").isPresent());
        assertEquals(5, repositorio.buscarDisponibles(new FiltroDePresentaciones(null, null, null, null, null), 0, 10)
                .totalElementos());
    }
}
