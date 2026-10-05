package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

/**
 * Caso de uso: PublicarPresentacion.
 * (Actor: Caficultor, Tostador o Vendedor de Derivados) — publica una
 * Presentación Trazable; exige Origen (Lote o Transformación) y valida el
 * Perfil de Tueste según el rol. El Origen debe existir y se le descuenta la
 * cantidad publicada: al Lote si es café sin tostar, a la Transformación si es
 * tostado o derivado. Si el Origen no existe o no alcanza, la Presentación no
 * se guarda.
 */
public class PublicarPresentacion {

    private final PresentacionRepositorio presentacionRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final TransformacionRepositorio transformacionRepositorio;

    public PublicarPresentacion(PresentacionRepositorio presentacionRepositorio, LoteRepositorio loteRepositorio,
                                 TransformacionRepositorio transformacionRepositorio) {
        this.presentacionRepositorio = presentacionRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.transformacionRepositorio = transformacionRepositorio;
    }

    public Presentacion ejecutar(String id, String vendedorId, String loteId, String transformacionId,
                                  String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                  Cantidad cantidadDisponible, Galeria galeria, PerfilTueste perfilDeTueste,
                                  RolVendedor rolVendedor, FechaDeTueste fechaTueste) {
        Presentacion presentacion = Presentacion.publicar(id, vendedorId, loteId, transformacionId, titulo,
                tipoPresentacion, precio, cantidadDisponible, galeria, perfilDeTueste, rolVendedor, fechaTueste,
                null, null);

        if (presentacion instanceof PresentacionTrazable trazable) {
            OrigenDePresentacion origen = trazable.getOrigen();
            if (origen.loteId() != null) {
                CodigoDeLote codigo = new CodigoDeLote(origen.loteId());
                Lote lote = loteRepositorio.buscarPorCodigo(codigo)
                        .orElseThrow(() -> new ReglaDominioException(
                                "No existe un Lote con código " + codigo.valor()));

                lote.descontar(cantidadDisponible);
                loteRepositorio.guardar(lote);
            } else {
                String idTransformacion = origen.transformacionId();
                Transformacion transformacion = transformacionRepositorio.buscarPorId(idTransformacion)
                        .orElseThrow(() -> new ReglaDominioException(
                                "No existe una Transformación con id " + idTransformacion));

                transformacion.asignarCantidad(cantidadDisponible);
                transformacionRepositorio.guardar(transformacion);
            }
        }

        presentacionRepositorio.guardar(presentacion);
        return presentacion;
    }
}
