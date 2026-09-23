package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
}
