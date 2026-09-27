package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.FichaDeOrigen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConsultarFichaOrigen {

    private final TransformacionRepositorio transformacionRepositorio;
    private final LoteRepositorio loteRepositorio;

    public ConsultarFichaOrigen(TransformacionRepositorio transformacionRepositorio, LoteRepositorio loteRepositorio) {
        this.transformacionRepositorio = transformacionRepositorio;
        this.loteRepositorio = loteRepositorio;
    }

    public FichaDeOrigen ejecutar(String transformacionId) {
        Optional transformacionOpt = transformacionRepositorio.buscarPorId(transformacionId);

        if (transformacionOpt == null || transformacionOpt.isEmpty()) {
            throw new ReglaDominioException("No se encontró la transformación especificada: " + transformacionId);
        }

        // Casteo explícito a Transformacion para resolver el error de tipos en .get()
        Transformacion transformacion = (Transformacion) transformacionOpt.get();

        List lotes = new ArrayList<>();

        for (Object item : transformacion.getLoteIds()) {
            String codigoStr = item.toString();
            CodigoDeLote codigo = new CodigoDeLote(codigoStr);

            Optional loteOpt = loteRepositorio.buscarPorCodigo(codigo);
            if (loteOpt != null && loteOpt.isPresent()) {
                lotes.add(loteOpt.get());
            }
        }

        // Trazabilidad consolidada
        String fincaNombre = lotes.isEmpty() ? "Desconocida" : "Finca del Quindío";
        String cosechaInfo = lotes.isEmpty() ? "Sin Registro" : "Cosecha Principal";

        return new FichaDeOrigen(fincaNombre, cosechaInfo, lotes, transformacion);
    }
}