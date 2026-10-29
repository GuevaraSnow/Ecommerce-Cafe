package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.PresentacionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

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
@Import({PresentacionRepositorioJpa.class, PresentacionPersistenceMapper.class})
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
