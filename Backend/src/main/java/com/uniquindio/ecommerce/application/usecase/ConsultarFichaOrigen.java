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

public class ConsultarFichaOrigen {

    private final TransformacionRepositorio transformacionRepositorio;
    private final LoteRepositorio loteRepositorio;

    public ConsultarFichaOrigen(TransformacionRepositorio transformacionRepositorio, LoteRepositorio loteRepositorio) {
        this.transformacionRepositorio = transformacionRepositorio;
        this.loteRepositorio = loteRepositorio;
    }

    public FichaDeOrigen ejecutar(String transformacionId) {
        Transformacion transformacion = transformacionRepositorio.buscarPorId(transformacionId)
                .orElseThrow(() -> new ReglaDominioException("No se encontró la transformación especificada: " + transformacionId));

        List<Lote> lotes = new ArrayList<>();

        // Búsqueda del único Lote asociado a la Transformación
        CodigoDeLote codigo = new CodigoDeLote(transformacion.getLoteCodigo());
        loteRepositorio.buscarPorCodigo(codigo).ifPresent(lotes::add);

        // Trazabilidad consolidada
        String fincaNombre = lotes.isEmpty() ? "Desconocida" : "Finca del Quindío";
        String cosechaInfo = lotes.isEmpty() ? "Sin Registro" : "Cosecha Principal";

        return new FichaDeOrigen(fincaNombre, cosechaInfo, lotes, transformacion);
    }
}