package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

/**
 * Domain Service que registra un Lote nuevo. Genera su código (LOT-AAAA-NNN) con el siguiente número del
 * año de registro, que consulta al repositorio, y exige que la Cosecha sea del año de registro o del
 * anterior. No guarda: eso lo hace el caso de uso. Recibe el año como parámetro para no leer el reloj y
 * poder probarse con cualquier año.
 */
public class ServicioDeLote {

    private final LoteRepositorio loteRepositorio;

    public ServicioDeLote(LoteRepositorio loteRepositorio) {
        this.loteRepositorio = loteRepositorio;
    }

    /**
     * @param anio año de registro: define el código del Lote y la ventana de años permitida para su Cosecha
     */
    public Lote registrar(int anio, Cantidad cantidad, String fincaId, ProcesoDeBeneficio proceso,
                          EstadoDelCafe estado, Cosecha cosecha, VariedadDeCafe variedad){
        if (!cosecha.esRegistrable(anio)) {
            throw new ReglaDominioException(
                    "Solo se registran Cosechas del año en curso o del anterior: " + cosecha.anio());
        }
        CodigoDeLote codigoDeLote = CodigoDeLote.crear(anio,loteRepositorio.obtenerNumeroMasAlto(anio)+1);
        return Lote.registrar(codigoDeLote, cantidad, fincaId, proceso, estado, cosecha, variedad);
    }
}
