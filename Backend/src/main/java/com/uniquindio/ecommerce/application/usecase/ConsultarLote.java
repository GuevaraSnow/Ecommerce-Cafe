package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultarLote {

    private final LoteRepositorio loteRepositorio;

    @Transactional(readOnly = true)
    public Lote ejecutar(CodigoDeLote codigoDeLote) {
        return loteRepositorio.buscarPorCodigo(codigoDeLote).orElseThrow();
    }
}
