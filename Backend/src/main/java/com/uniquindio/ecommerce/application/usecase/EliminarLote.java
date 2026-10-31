package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: EliminarLote.
 * (Actor: Caficultor) — da de baja lógica un Lote. No tiene lógica: el borrado lógico es una
 * operación del propio Lote ({@link Lote#eliminarLogicamente()}), que conserva la fila para no romper
 * la trazabilidad de las Transformaciones y Presentaciones que ya lo usaron.
 */
@Service
@RequiredArgsConstructor
public class EliminarLote {

    private final LoteRepositorio loteRepositorio;

    @Transactional
    public Lote ejecutar(CodigoDeLote codigoDeLote) {
        Lote lote = loteRepositorio.buscarPorCodigo(codigoDeLote).orElseThrow();
        lote.eliminarLogicamente();
        loteRepositorio.guardar(lote);
        return lote;
    }
}
