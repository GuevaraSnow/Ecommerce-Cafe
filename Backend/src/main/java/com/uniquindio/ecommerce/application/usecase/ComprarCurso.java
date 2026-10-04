package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.CompraDigital;
import com.uniquindio.ecommerce.Domain.entity.Curso;
import com.uniquindio.ecommerce.Domain.entity.Inscripcion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.CursoRepositorio;
import com.uniquindio.ecommerce.Domain.repository.InscripcionRepositorio;

import java.time.LocalDate;

/**
 * Caso de uso: ComprarCurso.
 * (Actor: Comprador) — compra un Curso (variante digital de RealizarCompra);
 * bloquea la compra si ya tiene una Inscripción activa a ese Curso. Como no
 * hay paso de pago, la compra nace confirmada y deja creada la Inscripción.
 */
public class ComprarCurso {

    private final CursoRepositorio cursoRepositorio;
    private final InscripcionRepositorio inscripcionRepositorio;
    private final CompraRepositorio compraRepositorio;

    public ComprarCurso(CursoRepositorio cursoRepositorio, InscripcionRepositorio inscripcionRepositorio,
                        CompraRepositorio compraRepositorio) {
        this.cursoRepositorio = cursoRepositorio;
        this.inscripcionRepositorio = inscripcionRepositorio;
        this.compraRepositorio = compraRepositorio;
    }

    public CompraDigital ejecutar(String inscripcionId, String compradorId, String cursoId) {
        Curso curso = cursoRepositorio.buscarPorId(cursoId)
                .orElseThrow(() -> new ReglaDominioException("No existe un Curso con id " + cursoId));

        if (!curso.estaDisponibleParaVenta()) {
            throw new ReglaDominioException("El Curso no está disponible para la venta: " + cursoId);
        }
        if (inscripcionRepositorio.buscarActivaPorCompradorYCurso(compradorId, cursoId).isPresent()) {
            throw new ReglaDominioException("El Comprador ya tiene una Inscripción activa a este Curso");
        }

        CompraDigital compra = CompraDigital.iniciar(compradorId, cursoId, curso.getPrecio());
        compra.confirmar();
        Inscripcion inscripcion = Inscripcion.crear(inscripcionId, compradorId, cursoId, compra.getId(),
                LocalDate.now());

        compraRepositorio.guardar(compra);
        inscripcionRepositorio.guardar(inscripcion);
        return compra;
    }
}
