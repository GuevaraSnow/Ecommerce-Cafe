package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResenaRepositorioMemoriaTest {

    private final ResenaRepositorioMemoria repositorio = new ResenaRepositorioMemoria();

    private Resena resena(String id, String compradorId, String presentacionId) {
        return Resena.dejar(id, compradorId, presentacionId, "vendedor-1", new Calificacion(4),
                new TextoDeResena("Muy buen cafe"), LocalDateTime.of(2026, 10, 20, 18, 0));
    }

    @Test
    void guardaYBuscaUnaResenaPorSuId() {
        Resena resena = resena("resena-1", "comprador-1", "pres-1");

        repositorio.guardar(resena);

        assertEquals(resena, repositorio.buscarPorId("resena-1").orElseThrow());
        assertTrue(repositorio.buscarPorId("no-existe").isEmpty());
    }

    @Test
    void existeResenaDeCuandoEseCompradorYaResenoEsaPresentacion() {
        repositorio.guardar(resena("resena-1", "comprador-1", "pres-1"));

        assertTrue(repositorio.existeResenaDe("comprador-1", "pres-1"));
    }

    @Test
    void noCuentaLaResenaDeOtroCompradorNiDeOtraPresentacion() {
        repositorio.guardar(resena("resena-1", "comprador-2", "pres-1"));
        repositorio.guardar(resena("resena-2", "comprador-1", "pres-2"));

        assertFalse(repositorio.existeResenaDe("comprador-1", "pres-1"));
    }
}
