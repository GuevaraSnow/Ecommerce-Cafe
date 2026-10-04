package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.CompraDigital;
import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.entity.Leccion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.CompraRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.CursoRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.InscripcionRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComprarCursoTest {

    private CursoRepositorioMemoria cursos;
    private InscripcionRepositorioMemoria inscripciones;
    private CompraRepositorioMemoria compras;
    private ComprarCurso comprarCurso;

    @BeforeEach
    void preparar() {
        cursos = new CursoRepositorioMemoria();
        inscripciones = new InscripcionRepositorioMemoria();
        compras = new CompraRepositorioMemoria();
        comprarCurso = new ComprarCurso(cursos, inscripciones, compras);
    }

    private Curso publicarCurso(String cursoId) {
        Leccion leccion = Leccion.crear(cursoId, 1, "Introducción",
                new ArchivoDigital("http://video/1.mp4", TipoDeArchivo.MP4, new TamanoDeArchivo(1024)),
                new Duracion(10));
        return new PublicarCurso(cursos).ejecutar(cursoId, "formador-1", "Barismo Básico", "Fundamentos",
                new Precio(50000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.BARISMO, List.of(leccion));
    }

    @Test
    void comprarUnCursoCreaLaCompraConfirmadaYLaInscripcion() {
        publicarCurso("curso-1");

        CompraDigital compra = comprarCurso.ejecutar("insc-1", "comprador-1", "curso-1");

        assertEquals(EstadoDeCompra.CONFIRMADA, compra.getEstado());
        assertEquals("curso-1", compra.getCursoId());
        assertEquals("comprador-1", compra.getCompradorId());
        assertTrue(compras.buscarPorId(compra.getId()).isPresent());

        Inscripcion inscripcion = inscripciones.buscarActivaPorCompradorYCurso("comprador-1", "curso-1")
                .orElseThrow();
        assertEquals("insc-1", inscripcion.getId());
        assertEquals(compra.getId(), inscripcion.getCompraId());
    }

    @Test
    void laCompraCongelaElPrecioVigenteDelCurso() {
        Curso curso = publicarCurso("curso-1");

        CompraDigital compra = comprarCurso.ejecutar("insc-1", "comprador-1", "curso-1");
        curso.cambiarPrecio(new Precio(90000, "COP"));

        assertEquals(new Precio(50000, "COP"), compra.getPrecioCongelado());
    }

    @Test
    void noSePuedeComprarDosVecesElMismoCursoMientrasLaInscripcionEsteActiva() {
        publicarCurso("curso-1");
        comprarCurso.ejecutar("insc-1", "comprador-1", "curso-1");

        assertThrows(ReglaDominioException.class,
                () -> comprarCurso.ejecutar("insc-2", "comprador-1", "curso-1"));

        assertTrue(inscripciones.buscarPorId("insc-2").isEmpty());
    }

    @Test
    void despuesDeRevocarLaInscripcionSePuedeVolverAComprar() {
        publicarCurso("curso-1");
        comprarCurso.ejecutar("insc-1", "comprador-1", "curso-1");
        Inscripcion primera = inscripciones.buscarPorId("insc-1").orElseThrow();
        primera.revocar();
        inscripciones.guardar(primera);

        comprarCurso.ejecutar("insc-2", "comprador-1", "curso-1");

        assertEquals("insc-2", inscripciones.buscarActivaPorCompradorYCurso("comprador-1", "curso-1")
                .orElseThrow().getId());
    }

    @Test
    void otroCompradorSiPuedeComprarElMismoCurso() {
        publicarCurso("curso-1");
        comprarCurso.ejecutar("insc-1", "comprador-1", "curso-1");

        comprarCurso.ejecutar("insc-2", "comprador-2", "curso-1");

        assertTrue(inscripciones.buscarActivaPorCompradorYCurso("comprador-2", "curso-1").isPresent());
    }

    @Test
    void noSePuedeComprarUnCursoInexistente() {
        assertThrows(ReglaDominioException.class,
                () -> comprarCurso.ejecutar("insc-1", "comprador-1", "no-existe"));
    }

    @Test
    void noSePuedeComprarUnCursoEnBorrador() {
        cursos.guardar(Curso.crear("curso-2", "formador-1", "Borrador", "Sin publicar",
                new Precio(40000, "COP"), NivelDelCurso.BASICO, TematicaDelCurso.CATACION));

        assertThrows(ReglaDominioException.class,
                () -> comprarCurso.ejecutar("insc-1", "comprador-1", "curso-2"));

        assertTrue(inscripciones.buscarActivaPorCompradorYCurso("comprador-1", "curso-2").isEmpty());
    }

    @Test
    void noSePuedeComprarUnCursoEliminado() {
        Curso curso = publicarCurso("curso-1");
        curso.eliminar();

        assertThrows(ReglaDominioException.class,
                () -> comprarCurso.ejecutar("insc-1", "comprador-1", "curso-1"));
    }

    @Test
    void sinCompradorNoSeCreaNadaYLaInscripcionNoQuedaGuardada() {
        publicarCurso("curso-1");

        assertThrows(ReglaDominioException.class, () -> comprarCurso.ejecutar("insc-1", "  ", "curso-1"));

        assertTrue(inscripciones.buscarPorId("insc-1").isEmpty());
    }
}
