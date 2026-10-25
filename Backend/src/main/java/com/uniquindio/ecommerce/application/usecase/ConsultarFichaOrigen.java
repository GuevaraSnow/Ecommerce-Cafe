package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.FichaDeOrigen;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultarFichaOrigen {

    private final TransformacionRepositorio transformacionRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final FincaRepositorio fincaRepositorio;

    @Transactional(readOnly = true)
    public FichaDeOrigen ejecutar(String transformacionId) {
        Transformacion transformacion = transformacionRepositorio.buscarPorId(transformacionId)
                .orElseThrow(() -> new ReglaDominioException("No se encontró la transformación especificada: " + transformacionId));

        List<Lote> lotes = new ArrayList<>();
        CodigoDeLote codigo = new CodigoDeLote(transformacion.getLoteCodigo());

        Lote lote = loteRepositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReglaDominioException("No se encontró el Lote con código: " + transformacion.getLoteCodigo()));

        lotes.add(lote);

        // Búsqueda de la Finca real asociada al Lote
        Finca finca = fincaRepositorio.buscarPorId(lote.getFincaID())
                .orElseThrow(() -> new ReglaDominioException("No se encontró la Finca con id: " + lote.getFincaID()));

        // Datos reales armados directamente desde las entidades persistidas
        String fincaNombre = finca.getNombre();
        String cosechaInfo = "Año: " + lote.getCosecha().anio() + " - Temporada: " + lote.getCosecha().temporada();

        return new FichaDeOrigen(
                fincaNombre,
                cosechaInfo,
                lotes,
                transformacion.getId(),
                transformacion.getTostadorId(),
                transformacion.getFecha(),
                transformacion.getPerfilTuesteResultante()
        );
    }
}