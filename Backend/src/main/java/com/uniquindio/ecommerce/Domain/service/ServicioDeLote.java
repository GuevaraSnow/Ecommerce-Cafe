package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import java.util.Collection;

public class ServicioDeLote {

    private final LoteRepositorio loteRepositorio;

    public ServicioDeLote(LoteRepositorio loteRepositorio) {
        this.loteRepositorio = loteRepositorio;
    }

    public Lote registrar(int anio, Cantidad cantidad, String fincaId, ProcesoDeBeneficio proceso,
                          EstadoDelCafe estado, Cosecha cosecha, VariedadDeCafe variedad){
        Collection<Lote> lotes = loteRepositorio.obtenerTodos().stream().filter(lote -> lote.getCodigoDeLote().anio() == anio).toList();
        int numeroMasAlto = lotes.stream().mapToInt(lote -> lote.getCodigoDeLote().numero()).max().orElse(0);
        String numeroLote = nuevoNumero(numeroMasAlto);
        CodigoDeLote codigoDeLote = new CodigoDeLote("LOT-" + anio + "-" + numeroLote);

        return Lote.registrar(codigoDeLote, cantidad, fincaId, proceso, estado, cosecha, variedad);
    }

    private String nuevoNumero(int numero){
        int numeroSiguiente = numero+1;
        return String.format("%03d", numeroSiguiente);
    }
}
