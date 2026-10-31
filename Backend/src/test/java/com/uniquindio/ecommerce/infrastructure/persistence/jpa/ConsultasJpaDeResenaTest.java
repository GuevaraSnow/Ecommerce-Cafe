package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Consulta de reseñas por Presentación contra H2, con los datos de data-compra.sql. */
@SpringBootTest
class ConsultasJpaDeResenaTest {

    @Autowired
    private ResenaRepositorio resenaRepositorio;

    @Test
    void buscarPorPresentacionTraeSusResenasConLaRespuestaDelVendedor() {
        List<Resena> resenas = resenaRepositorio.buscarPorPresentacion("pres-cafe-1");

        assertEquals(List.of("resena-001"), resenas.stream().map(Resena::getId).toList());
        assertTrue(resenas.get(0).getRespuesta().isPresent());
    }

    @Test
    void unaPresentacionSinResenasDevuelveUnaListaVacia() {
        assertTrue(resenaRepositorio.buscarPorPresentacion("pres-taza-1").isEmpty());
    }
}
