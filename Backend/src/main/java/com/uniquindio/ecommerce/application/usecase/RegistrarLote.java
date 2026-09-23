package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

public class RegistrarLote {

    private final LoteRepositorio repositorio;

    public RegistrarLote(LoteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Lote ejecutar(CodigoDeLote codigoDeLote, Cantidad cantidadDisponible, String fincaId,
                          ProcesoDeBeneficio procesoDeBeneficio, EstadoDelCafe estado,
                          Cosecha cosecha, VariedadDeCafe variedad) {
        Lote lote = Lote.registrar(codigoDeLote, cantidadDisponible, fincaId, procesoDeBeneficio, estado, cosecha, variedad);
        repositorio.guardar(lote);
        return lote;
    }
}
