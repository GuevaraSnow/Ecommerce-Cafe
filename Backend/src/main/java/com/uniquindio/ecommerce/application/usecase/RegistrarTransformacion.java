package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;

import java.time.LocalDate;
import java.util.List;

public class RegistrarTransformacion {

    /**
     * Caso de uso: RegistrarTransformacion
     * Ejecuta el registro de una transformación validando datos e invocando
     * la fábrica del dominio Transformacion.registrar(...).
     */
    public Transformacion ejecutar(
            String id,
            List<String> loteIds,
            Cantidad cantidadUsada,
            String tostadorId,
            LocalDate fecha,
            PerfilTueste perfilTuesteResultante
    ) {
        // Invoca el método estático fábrica de la Entidad Transformación de tu proyecto
        return Transformacion.registrar(
                id,
                loteIds,
                cantidadUsada,
                tostadorId,
                fecha,
                perfilTuesteResultante
        );
    }
}