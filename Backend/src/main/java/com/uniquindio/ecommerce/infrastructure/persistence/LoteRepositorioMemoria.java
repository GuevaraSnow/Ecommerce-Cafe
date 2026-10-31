package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;

import java.util.*;

//@Repository
public class LoteRepositorioMemoria implements LoteRepositorio {

    private final Map<String, Lote> lotes = new HashMap<>();

    @Override
    public Optional<Lote> buscarPorCodigo(CodigoDeLote codigo) {
        return Optional.ofNullable(lotes.get(codigo.valor()));
    }

    @Override
    public void guardar(Lote lote) {
        lotes.put(lote.getCodigoDeLote().valor(), lote);
    }

    @Override
    public int obtenerNumeroMasAlto(int anio) {
        Collection<Lote> lotesAnio = lotes.values().stream().filter(lote -> lote.getCodigoDeLote().anio() == anio).toList();
        return lotesAnio.stream().mapToInt(lote -> lote.getCodigoDeLote().numero()).max().orElse(0);
    }
}
