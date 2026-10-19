package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.OrigenDePresentacion;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;

/**
 * Pieza interna de los Domain Services que mueven café hacia una Presentación
 * (publicar y reponer): descuenta la cantidad del Origen, que es el Lote si el
 * café va sin tostar o la Transformación si es tostado o derivado. Cada raíz
 * protege su propia cantidad; aquí solo se decide a cuál pedírselo.
 * No es un bean: la crean los servicios que la usan.
 */
final class DescuentoDeOrigen {

    private final LoteRepositorio loteRepositorio;
    private final TransformacionRepositorio transformacionRepositorio;

    DescuentoDeOrigen(LoteRepositorio loteRepositorio, TransformacionRepositorio transformacionRepositorio) {
        this.loteRepositorio = loteRepositorio;
        this.transformacionRepositorio = transformacionRepositorio;
    }

    /**
     * @param perfil si no es nulo, debe coincidir con el Perfil de Tueste de la
     *               Transformación de origen
     * @throws ReglaDominioException si el Origen no existe, no alcanza o el perfil no coincide
     */
    void descontar(OrigenDePresentacion origen, Cantidad cantidad, PerfilTueste perfil) {
        if (origen.loteId() != null) {
            descontarDelLote(new CodigoDeLote(origen.loteId()), cantidad);
        } else {
            descontarDeLaTransformacion(origen.transformacionId(), cantidad, perfil);
        }
    }

    private void descontarDelLote(CodigoDeLote codigo, Cantidad cantidad) {
        Lote lote = loteRepositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ReglaDominioException("No existe un Lote con código " + codigo.valor()));
        lote.descontar(cantidad);
        loteRepositorio.guardar(lote);
    }

    private void descontarDeLaTransformacion(String transformacionId, Cantidad cantidad, PerfilTueste perfil) {
        Transformacion transformacion = transformacionRepositorio.buscarPorId(transformacionId)
                .orElseThrow(() -> new ReglaDominioException(
                        "No existe una Transformación con id " + transformacionId));
        if (perfil != null && perfil != transformacion.getPerfilTuesteResultante()) {
            throw new ReglaDominioException("El Perfil de Tueste de la Presentación (" + perfil
                    + ") no coincide con el de la Transformación (" + transformacion.getPerfilTuesteResultante() + ")");
        }
        transformacion.asignarCantidad(cantidad);
        transformacionRepositorio.guardar(transformacion);
    }
}
