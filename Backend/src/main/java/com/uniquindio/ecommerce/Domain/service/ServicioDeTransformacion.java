package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;

import java.time.LocalDate;

public class ServicioDeTransformacion {

    private final TransformacionRepositorio transformacionRepositorio;
    private final LoteRepositorio loteRepositorio;

    public ServicioDeTransformacion(TransformacionRepositorio transformacionRepositorio,
                                    LoteRepositorio loteRepositorio) {
        this.transformacionRepositorio = transformacionRepositorio;
        this.loteRepositorio = loteRepositorio;
    }

    public Transformacion transformar(
            String id,
            String loteCodigo,
            Cantidad cantidadEntrada,
            PorcentajeDeMerma merma,
            String tostadorId,
            LocalDate fecha,
            PerfilTueste perfilTuesteResultante
    ) {
        Transformacion transformacion = Transformacion.registrar(
                id,
                loteCodigo,
                cantidadEntrada,
                merma,
                tostadorId,
                fecha,
                perfilTuesteResultante
        );

        CodigoDeLote codigo = new CodigoDeLote(loteCodigo);
        Lote lote = loteRepositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el Lote con código: " + loteCodigo));

        lote.descontar(cantidadEntrada);

        loteRepositorio.guardar(lote);
        transformacionRepositorio.guardar(transformacion);

        return transformacion;
    }
}