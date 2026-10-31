package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeTransformacion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Caso de uso: RegistrarTransformacion.
 * (Actor: Tostador) — carga el Lote (no encontrado si no existe), deja que el Domain Service
 * {@link ServicioDeTransformacion} aplique la regla y guarda el Lote y la Transformación. No tiene lógica de negocio.
 */
@Service
@RequiredArgsConstructor
public class RegistrarTransformacion {

    private final ServicioDeTransformacion servicioDeTransformacion;
    private final LoteRepositorio loteRepositorio;
    private final TransformacionRepositorio transformacionRepositorio;

    @Transactional
    public Transformacion ejecutar(
            String id,
            CodigoDeLote codigoDeLote,
            Cantidad cantidadEntrada,
            PorcentajeDeMerma merma,
            String tostadorId,
            LocalDate fecha,
            PerfilTueste perfilTuesteResultante
    ) {
        Lote lote = loteRepositorio.buscarPorCodigo(codigoDeLote).orElseThrow();
        Transformacion transformacion = servicioDeTransformacion.transformar(
                id,
                lote,
                cantidadEntrada,
                merma,
                tostadorId,
                fecha,
                perfilTuesteResultante
        );
        loteRepositorio.guardar(lote);
        transformacionRepositorio.guardar(transformacion);
        return transformacion;
    }
}
