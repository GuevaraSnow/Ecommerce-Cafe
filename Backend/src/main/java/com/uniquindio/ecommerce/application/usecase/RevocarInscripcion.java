package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.InscripcionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;

import java.time.LocalDate;

/**
 * Caso de uso: RevocarInscripcion.
 * (Actor: Sistema) — al aprobarse una Solicitud de Reembolso de un Curso,
 * aprueba la solicitud y revoca la Inscripción asociada.
 */
public class RevocarInscripcion {

    private final SolicitudReembolsoRepositorio solicitudRepositorio;
    private final InscripcionRepositorio inscripcionRepositorio;

    public RevocarInscripcion(SolicitudReembolsoRepositorio solicitudRepositorio,
                               InscripcionRepositorio inscripcionRepositorio) {
        this.solicitudRepositorio = solicitudRepositorio;
        this.inscripcionRepositorio = inscripcionRepositorio;
    }

    public Inscripcion ejecutar(String solicitudReembolsoId, String inscripcionId) {
        SolicitudReembolso solicitud = solicitudRepositorio.buscarPorId(solicitudReembolsoId)
                .orElseThrow(() -> new ReglaDominioException(
                        "No existe una Solicitud de Reembolso con id " + solicitudReembolsoId));
        Inscripcion inscripcion = inscripcionRepositorio.buscarPorId(inscripcionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Inscripción con id " + inscripcionId));

        solicitud.aprobar(LocalDate.now());
        inscripcion.revocar();

        solicitudRepositorio.guardar(solicitud);
        inscripcionRepositorio.guardar(inscripcion);
        return inscripcion;
    }
}
