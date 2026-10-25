package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.service.ServicioDeTransformacion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RegistrarTransformacion {

    private final ServicioDeTransformacion servicioDeTransformacion;

    @Transactional
    public Transformacion ejecutar(
            String id,
            String loteCodigo,
            Cantidad cantidadEntrada,
            PorcentajeDeMerma merma,
            String tostadorId,
            LocalDate fecha,
            PerfilTueste perfilTuesteResultante
    ) {
        return servicioDeTransformacion.transformar(
                id,
                loteCodigo,
                cantidadEntrada,
                merma,
                tostadorId,
                fecha,
                perfilTuesteResultante
        );
    }
}