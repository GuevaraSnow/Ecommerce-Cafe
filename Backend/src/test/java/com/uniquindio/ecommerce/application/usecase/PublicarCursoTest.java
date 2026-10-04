package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.entity.Leccion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.CursoRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PublicarCursoTest {

    private Leccion leccionValida(String cursoId, int orden) {
        return Leccion.crear(cursoId, orden, "Introducción",
                new ArchivoDigital("http://video/leccion-" + orden + ".mp4", TipoDeArchivo.MP4,
                        new TamanoDeArchivo(1024)),
                new Duracion(10));
    }

    @Test
    void publicaUnCursoConAlMenosUnaLeccion() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        PublicarCurso publicar = new PublicarCurso(repositorio);

        Curso curso = publicar.ejecutar("curso-1", "formador-1", "Barismo Básico", "Fundamentos del café",
                new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO,
                List.of(leccionValida("curso-1", 1)));

        assertEquals(EstadoDePublicacion.ACTIVA, curso.getEstado());
        assertEquals(1, curso.getLecciones().size());
        assertTrue(repositorio.buscarPorId("curso-1").isPresent());
    }

    @Test
    void noSePuedePublicarUnCursoSinLecciones() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        PublicarCurso publicar = new PublicarCurso(repositorio);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("curso-2", "formador-1", "Barismo Básico",
                "Fundamentos del café", new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO,
                List.of()));
    }

    @Test
    void noSePuedePublicarUnCursoConListaDeLeccionesNula() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        PublicarCurso publicar = new PublicarCurso(repositorio);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("curso-3", "formador-1", "Barismo Básico",
                "Fundamentos del café", new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO,
                null));
    }

    @Test
    void noSePuedePublicarSinFormador() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        PublicarCurso publicar = new PublicarCurso(repositorio);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("curso-4", " ", "Barismo Básico",
                "Fundamentos del café", new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO,
                List.of(leccionValida("curso-4", 1))));
    }

    @Test
    void unaLeccionQueNoPerteneceAlCursoFalla() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        PublicarCurso publicar = new PublicarCurso(repositorio);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("curso-5", "formador-1", "Barismo Básico",
                "Fundamentos del café", new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO,
                List.of(leccionValida("otro-curso", 1))));
    }
}
