package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.*;

public class RegistrarLote {

    public Lote ejecutar(CodigoDeLote codigoDeLote, Cantidad cantidadDisponible, String fincaId,
                          ProcesoDeBeneficio procesoDeBeneficio, EstadoDelCafe estado,
                          Cosecha cosecha, VariedadDeCafe variedad) {
        return Lote.registrar(codigoDeLote, cantidadDisponible, fincaId, procesoDeBeneficio, estado, cosecha, variedad);
    }
}
