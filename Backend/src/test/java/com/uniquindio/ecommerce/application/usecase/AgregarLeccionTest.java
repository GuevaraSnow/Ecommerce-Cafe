package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.entity.Leccion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.CursoRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgregarLeccionTest {

    private ArchivoDigital archivoValido(int n) {
        return new ArchivoDigital("http://video/leccion-" + n + ".mp4", TipoDeArchivo.MP4, new TamanoDeArchivo(1024));
    }

    private Curso publicarCursoConUnaLeccion(CursoRepositorioMemoria repositorio, String cursoId) {
        PublicarCurso publicar = new PublicarCurso(repositorio);
        Leccion primera = Leccion.crear(cursoId, 1, "Introducción", archivoValido(1), new Duracion(10));
        return publicar.ejecutar(cursoId, "formador-1", "Barismo Básico", "Fundamentos del café",
                new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO, List.of(primera));
    }

    @Test
    void agregarAlFinalNoRenumeraLasExistentes() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        Curso curso = publicarCursoConUnaLeccion(repositorio, "curso-1");
        AgregarLeccion agregar = new AgregarLeccion(repositorio);

        agregar.ejecutar("curso-1", 2, "Molienda", archivoValido(2), new Duracion(15));

        List<Leccion> lecciones = repositorio.buscarPorId("curso-1").get().getLecciones();
        assertEquals(2, lecciones.size());
        assertEquals("Introducción", lecciones.get(0).getTitulo());
        assertEquals(1, lecciones.get(0).getNumeroOrden());
        assertEquals("Molienda", lecciones.get(1).getTitulo());
        assertEquals(2, lecciones.get(1).getNumeroOrden());
    }

    @Test
    void insertarAlPrincipioRenumeraLasExistentes() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        publicarCursoConUnaLeccion(repositorio, "curso-2");
        AgregarLeccion agregar = new AgregarLeccion(repositorio);

        agregar.ejecutar("curso-2", 1, "Bienvenida", archivoValido(2), new Duracion(5));

        List<Leccion> lecciones = repositorio.buscarPorId("curso-2").get().getLecciones();
        assertEquals(2, lecciones.size());
        assertEquals("Bienvenida", lecciones.get(0).getTitulo());
        assertEquals(1, lecciones.get(0).getNumeroOrden());
        assertEquals("Introducción", lecciones.get(1).getTitulo());
        assertEquals(2, lecciones.get(1).getNumeroOrden());
    }

    @Test
    void posicionMenorAUnoFalla() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        publicarCursoConUnaLeccion(repositorio, "curso-3");
        AgregarLeccion agregar = new AgregarLeccion(repositorio);

        assertThrows(ReglaDominioException.class,
                () -> agregar.ejecutar("curso-3", 0, "Bienvenida", archivoValido(2), new Duracion(5)));
    }

    @Test
    void posicionMuyLejosDelFinalFalla() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        publicarCursoConUnaLeccion(repositorio, "curso-4");
        AgregarLeccion agregar = new AgregarLeccion(repositorio);

        assertThrows(ReglaDominioException.class,
                () -> agregar.ejecutar("curso-4", 5, "Bienvenida", archivoValido(2), new Duracion(5)));
    }

    @Test
    void lanzaExcepcionSiElCursoNoExiste() {
        CursoRepositorioMemoria repositorio = new CursoRepositorioMemoria();
        AgregarLeccion agregar = new AgregarLeccion(repositorio);

        assertThrows(ReglaDominioException.class,
                () -> agregar.ejecutar("no-existe", 1, "Bienvenida", archivoValido(1), new Duracion(5)));
    }
}
