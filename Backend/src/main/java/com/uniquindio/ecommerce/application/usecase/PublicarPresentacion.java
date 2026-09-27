package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

/**
 * Caso de uso: PublicarPresentacion.
 * (Actor: Caficultor, Tostador o Vendedor de Derivados) — publica una
 * Presentación Trazable; exige Origen (Lote o Transformación), valida el
 * Perfil de Tueste según el rol y que la cantidad no supere la cantidad
 * resultante disponible de la Transformación.
 */
public class PublicarPresentacion {

    private final PresentacionRepositorio presentacionRepositorio;
    private final TransformacionRepositorio transformacionRepositorio;

    public PublicarPresentacion(PresentacionRepositorio presentacionRepositorio,
                                 TransformacionRepositorio transformacionRepositorio) {
        this.presentacionRepositorio = presentacionRepositorio;
        this.transformacionRepositorio = transformacionRepositorio;
    }

    public Presentacion ejecutar(String id, String vendedorId, String loteId, String transformacionId,
                                  String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                  Cantidad cantidadDisponible, Galeria galeria, PerfilTueste perfilDeTueste,
                                  RolVendedor rolVendedor, FechaDeTueste fechaTueste) {
        Presentacion presentacion = Presentacion.publicar(id, vendedorId, loteId, transformacionId, titulo,
                tipoPresentacion, precio, cantidadDisponible, galeria, perfilDeTueste, rolVendedor, fechaTueste,
                null, null);

        if (presentacion instanceof PresentacionTrazable trazable
                && trazable.getOrigen().transformacionId() != null) {
            String idTransformacion = trazable.getOrigen().transformacionId();
            Transformacion transformacion = transformacionRepositorio.buscarPorId(idTransformacion)
                    .orElseThrow(() -> new ReglaDominioException(
                            "No existe una Transformación con id " + idTransformacion));

            transformacion.asignarCantidad(cantidadDisponible);
            transformacionRepositorio.guardar(transformacion);
        }

        presentacionRepositorio.guardar(presentacion);
        return presentacion;
    }
}
