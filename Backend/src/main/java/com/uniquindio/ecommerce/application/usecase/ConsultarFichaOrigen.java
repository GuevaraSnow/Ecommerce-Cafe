package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.FichaDeOrigen;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: ConsultarFichaOrigen.
 * (Actor: cualquier usuario autenticado) — arma la Ficha de Origen de una Transformación con su Lote y su
 * Finca reales; si falta alguno, no encontrado. No modifica nada.
 */
@Service
@RequiredArgsConstructor
public class ConsultarFichaOrigen {

    private final TransformacionRepositorio transformacionRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final FincaRepositorio fincaRepositorio;

    @Transactional(readOnly = true)
    public FichaDeOrigen ejecutar(String transformacionId) {
        Transformacion transformacion = transformacionRepositorio.buscarPorId(transformacionId).orElseThrow();
        Lote lote = loteRepositorio.buscarPorCodigo(new CodigoDeLote(transformacion.getLoteCodigo())).orElseThrow();
        Finca finca = fincaRepositorio.buscarPorId(lote.getFincaID()).orElseThrow();

        return new FichaDeOrigen(
                finca.getNombre(),
                lote.getCosecha(),
                List.of(lote),
                transformacion.getId(),
                transformacion.getTostadorId(),
                transformacion.getFecha(),
                transformacion.getPerfilTuesteResultante()
        );
    }
}
