package com.uniquindio.ecommerce.application.usecase;

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
import java.util.ArrayList;
import java.util.List;

public class RegistrarTransformacion {

    private final TransformacionRepositorio transformacionRepositorio;
    private final LoteRepositorio loteRepositorio;

    public RegistrarTransformacion(TransformacionRepositorio transformacionRepositorio, LoteRepositorio loteRepositorio) {
        this.transformacionRepositorio = transformacionRepositorio;
        this.loteRepositorio = loteRepositorio;
    }

    public Transformacion ejecutar(
            String id,
            List loteIds,
            Cantidad cantidadEntrada,
            PorcentajeDeMerma merma,
            String tostadorId,
            LocalDate fecha,
            PerfilTueste perfilTuesteResultante
    ) {
        if (loteIds == null || loteIds.isEmpty()) {
            throw new ReglaDominioException("La Transformación debe utilizar al menos un Lote de origen.");
        }

        List codigosTexto = new ArrayList<>();

        for (Object item : loteIds) {
            String codigoStr = item.toString();
            codigosTexto.add(codigoStr);

            CodigoDeLote codigo = new CodigoDeLote(codigoStr);

            Lote lote = loteRepositorio.buscarPorCodigo(codigo)
                    .orElseThrow(() -> new ReglaDominioException("No se encontró el Lote con código: " + codigoStr));

            lote.descontar(cantidadEntrada);
            loteRepositorio.guardar(lote);
        }

        Transformacion transformacion = Transformacion.registrar(
                id,
                codigosTexto,
                cantidadEntrada,
                merma,
                tostadorId,
                fecha,
                perfilTuesteResultante
        );

        transformacionRepositorio.guardar(transformacion);

        return transformacion;
    }
}