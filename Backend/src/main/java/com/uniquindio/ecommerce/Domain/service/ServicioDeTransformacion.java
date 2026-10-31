package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;

import java.time.LocalDate;

/**
 * Domain Service que registra una Transformación: la crea (aplicando la merma) y descuenta del Lote la
 * cantidad de entrada. Es la regla que cruza a la Transformación y al Lote. Recibe el Lote ya cargado y no
 * guarda nada: cargar (y responder no encontrado) y guardar son del caso de uso.
 */
public class ServicioDeTransformacion {

    /**
     * @throws com.uniquindio.ecommerce.Domain.exception.ReglaDominioException si faltan datos, la cantidad
     *         de entrada supera la disponible del Lote o el Lote está dado de baja; en ese caso el Lote no cambia
     */
    public Transformacion transformar(
            String id,
            Lote lote,
            Cantidad cantidadEntrada,
            PorcentajeDeMerma merma,
            String tostadorId,
            LocalDate fecha,
            PerfilTueste perfilTuesteResultante
    ) {
        Transformacion transformacion = Transformacion.registrar(
                id,
                lote.getCodigoDeLote().valor(),
                cantidadEntrada,
                merma,
                tostadorId,
                fecha,
                perfilTuesteResultante
        );
        lote.descontar(cantidadEntrada);
        return transformacion;
    }
}
