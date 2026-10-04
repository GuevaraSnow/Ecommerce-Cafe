package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.entity.Leccion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.CursoRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.InscripcionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccederLeccionTest {

    private ArchivoDigital archivoValido() {
        return new ArchivoDigital("http://video/leccion-1.mp4", TipoDeArchivo.MP4, new TamanoDeArchivo(1024));
    }

    private void publicarCursoConUnaLeccion(CursoRepositorioMemoria repositorio, String cursoId) {
        Leccion leccion = Leccion.crear(cursoId, 1, "Introducción", archivoValido(), new Duracion(10));
        new PublicarCurso(repositorio).ejecutar(cursoId, "formador-1", "Barismo Básico", "Fundamentos del café",
                new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO, List.of(leccion));
    }

    @Test
    void unCompradorConInscripcionVigenteAccedeALaLeccion() {
        CursoRepositorioMemoria cursos = new CursoRepositorioMemoria();
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        publicarCursoConUnaLeccion(cursos, "curso-1");
        Inscripcion inscripcion = Inscripcion.crear("insc-1", "comprador-1", "curso-1", "compra-1", LocalDate.now());
        inscripciones.guardar(inscripcion);
        AccederLeccion acceder = new AccederLeccion(inscripciones, cursos);

        Leccion leccion = acceder.ejecutar("comprador-1", "curso-1", 1);

        assertEquals("Introducción", leccion.getTitulo());
        assertTrue(inscripciones.buscarPorId("insc-1").get().isAccedido());
    }

    @Test
    void sinInscripcionNoSePuedeAcceder() {
        CursoRepositorioMemoria cursos = new CursoRepositorioMemoria();
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        publicarCursoConUnaLeccion(cursos, "curso-2");
        AccederLeccion acceder = new AccederLeccion(inscripciones, cursos);

        assertThrows(ReglaDominioException.class, () -> acceder.ejecutar("comprador-1", "curso-2", 1));
    }

    @Test
    void unaInscripcionRevocadaNoDaAcceso() {
        CursoRepositorioMemoria cursos = new CursoRepositorioMemoria();
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        publicarCursoConUnaLeccion(cursos, "curso-3");
        Inscripcion revocada = Inscripcion.crear("insc-2", "comprador-1", "curso-3", "compra-1", LocalDate.now());
        revocada.revocar();
        inscripciones.guardar(revocada);
        AccederLeccion acceder = new AccederLeccion(inscripciones, cursos);

        assertThrows(ReglaDominioException.class, () -> acceder.ejecutar("comprador-1", "curso-3", 1));
    }

    @Test
    void soloLaInscripcionVigenteDaAccesoCuandoHayUnaRevocadaYOtraActiva() {
        CursoRepositorioMemoria cursos = new CursoRepositorioMemoria();
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        publicarCursoConUnaLeccion(cursos, "curso-4");
        Inscripcion revocada = Inscripcion.crear("insc-3", "comprador-1", "curso-4", "compra-1", LocalDate.now());
        revocada.revocar();
        inscripciones.guardar(revocada);
        Inscripcion vigente = Inscripcion.crear("insc-4", "comprador-1", "curso-4", "compra-2", LocalDate.now());
        inscripciones.guardar(vigente);
        AccederLeccion acceder = new AccederLeccion(inscripciones, cursos);

        Leccion leccion = acceder.ejecutar("comprador-1", "curso-4", 1);

        assertEquals("Introducción", leccion.getTitulo());
        assertTrue(inscripciones.buscarPorId("insc-4").get().isAccedido());
        assertFalse(inscripciones.buscarPorId("insc-3").get().isAccedido());
    }

    @Test
    void unNumeroDeOrdenQueNoExisteFalla() {
        CursoRepositorioMemoria cursos = new CursoRepositorioMemoria();
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        publicarCursoConUnaLeccion(cursos, "curso-5");
        inscripciones.guardar(Inscripcion.crear("insc-5", "comprador-1", "curso-5", "compra-1", LocalDate.now()));
        AccederLeccion acceder = new AccederLeccion(inscripciones, cursos);

        assertThrows(ReglaDominioException.class, () -> acceder.ejecutar("comprador-1", "curso-5", 99));
    }
}
