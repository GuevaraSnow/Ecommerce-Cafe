package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import com.uniquindio.ecommerce.infrastructure.persistence.InscripcionRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.SolicitudReembolsoRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolicitarReembolsoCursoTest {

    @Test
    void solicitaReembolsoDeUnaInscripcionReciente() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        inscripciones.guardar(Inscripcion.crear("insc-1", "comprador-1", "curso-1", "compra-1", LocalDate.now()));
        SolicitarReembolsoCurso solicitar = new SolicitarReembolsoCurso(inscripciones, solicitudes);

        SolicitudReembolso solicitud = solicitar.ejecutar("solicitud-1", "insc-1", "Ya no me interesa el curso");

        assertEquals(EstadoDeReembolso.SOLICITADO, solicitud.getEstado());
        assertEquals("compra-1", solicitud.getCompraId());
        assertTrue(solicitudes.buscarPorId("solicitud-1").isPresent());
    }

    @Test
    void noSePuedeSolicitarReembolsoSiYaAccedioAlContenido() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        Inscripcion inscripcion = Inscripcion.crear("insc-2", "comprador-1", "curso-1", "compra-2", LocalDate.now());
        inscripcion.registrarAcceso(LocalDate.now());
        inscripciones.guardar(inscripcion);
        SolicitarReembolsoCurso solicitar = new SolicitarReembolsoCurso(inscripciones, solicitudes);

        assertThrows(ReglaDominioException.class,
                () -> solicitar.ejecutar("solicitud-2", "insc-2", "Cambié de opinión"));
    }

    @Test
    void noSePuedeSolicitarReembolsoPasadasLas48Horas() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        Inscripcion inscripcion = Inscripcion.crear("insc-3", "comprador-1", "curso-1", "compra-3",
                LocalDate.now().minusDays(5));
        inscripciones.guardar(inscripcion);
        SolicitarReembolsoCurso solicitar = new SolicitarReembolsoCurso(inscripciones, solicitudes);

        assertThrows(ReglaDominioException.class,
                () -> solicitar.ejecutar("solicitud-3", "insc-3", "Cambié de opinión"));
    }

    @Test
    void lanzaExcepcionSiLaInscripcionNoExiste() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        SolicitarReembolsoCurso solicitar = new SolicitarReembolsoCurso(inscripciones, solicitudes);

        assertThrows(ReglaDominioException.class,
                () -> solicitar.ejecutar("solicitud-4", "no-existe", "Motivo"));
    }
}
