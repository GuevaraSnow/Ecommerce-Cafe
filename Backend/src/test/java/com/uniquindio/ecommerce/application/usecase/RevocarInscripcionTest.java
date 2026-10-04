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

class RevocarInscripcionTest {

    @Test
    void apruebaLaSolicitudYRevocaLaInscripcion() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        inscripciones.guardar(Inscripcion.crear("insc-1", "comprador-1", "curso-1", "compra-1", LocalDate.now()));
        solicitudes.guardar(SolicitudReembolso.solicitar("solicitud-1", "compra-1", "Motivo", LocalDate.now()));
        RevocarInscripcion revocar = new RevocarInscripcion(solicitudes, inscripciones);

        Inscripcion inscripcion = revocar.ejecutar("solicitud-1", "insc-1");

        assertTrue(inscripcion.isRevocada());
        assertEquals(EstadoDeReembolso.APROBADO, solicitudes.buscarPorId("solicitud-1").get().getEstado());
    }

    @Test
    void lanzaExcepcionSiLaSolicitudNoExiste() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        inscripciones.guardar(Inscripcion.crear("insc-2", "comprador-1", "curso-1", "compra-2", LocalDate.now()));
        RevocarInscripcion revocar = new RevocarInscripcion(solicitudes, inscripciones);

        assertThrows(ReglaDominioException.class, () -> revocar.ejecutar("no-existe", "insc-2"));
    }

    @Test
    void lanzaExcepcionSiLaInscripcionNoExiste() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        solicitudes.guardar(SolicitudReembolso.solicitar("solicitud-2", "compra-3", "Motivo", LocalDate.now()));
        RevocarInscripcion revocar = new RevocarInscripcion(solicitudes, inscripciones);

        assertThrows(ReglaDominioException.class, () -> revocar.ejecutar("solicitud-2", "no-existe"));
    }

    @Test
    void noSePuedeResolverDosVecesLaMismaSolicitud() {
        InscripcionRepositorioMemoria inscripciones = new InscripcionRepositorioMemoria();
        SolicitudReembolsoRepositorioMemoria solicitudes = new SolicitudReembolsoRepositorioMemoria();
        inscripciones.guardar(Inscripcion.crear("insc-3", "comprador-1", "curso-1", "compra-4", LocalDate.now()));
        solicitudes.guardar(SolicitudReembolso.solicitar("solicitud-3", "compra-4", "Motivo", LocalDate.now()));
        RevocarInscripcion revocar = new RevocarInscripcion(solicitudes, inscripciones);
        revocar.ejecutar("solicitud-3", "insc-3");

        assertThrows(ReglaDominioException.class, () -> revocar.ejecutar("solicitud-3", "insc-3"));
    }
}
