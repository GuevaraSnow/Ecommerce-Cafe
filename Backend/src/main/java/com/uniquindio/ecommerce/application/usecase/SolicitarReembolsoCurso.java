package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.InscripcionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;

import java.time.LocalDate;

/**
 * Caso de uso: SolicitarReembolsoCurso.
 * (Actor: Comprador) — pide reembolso de un Curso dentro de las 48 horas
 * posteriores a la compra y antes de acceder al contenido.
 */
public class SolicitarReembolsoCurso {

    private final InscripcionRepositorio inscripcionRepositorio;
    private final SolicitudReembolsoRepositorio solicitudRepositorio;

    public SolicitarReembolsoCurso(InscripcionRepositorio inscripcionRepositorio,
                                    SolicitudReembolsoRepositorio solicitudRepositorio) {
        this.inscripcionRepositorio = inscripcionRepositorio;
        this.solicitudRepositorio = solicitudRepositorio;
    }

    public SolicitudReembolso ejecutar(String solicitudId, String inscripcionId, String motivo) {
        Inscripcion inscripcion = inscripcionRepositorio.buscarPorId(inscripcionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Inscripción con id " + inscripcionId));

        if (!inscripcion.puedeReembolsarse()) {
            throw new ReglaDominioException(
                    "La Inscripción ya no puede reembolsarse: pasaron las 48 horas o ya se accedió al contenido");
        }

        SolicitudReembolso solicitud = SolicitudReembolso.solicitar(solicitudId, inscripcion.getCompraId(), motivo,
                LocalDate.now());
        solicitudRepositorio.guardar(solicitud);
        return solicitud;
    }
}
