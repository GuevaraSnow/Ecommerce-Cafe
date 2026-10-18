package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

/**
 * Domain Service que orquesta la publicación de una Presentación Trazable.
 * La regla cruza agregados: la Presentación no puede publicar más café del que
 * tiene su Origen (regla 13), y ese Origen es otro agregado (Lote o
 * Transformación). Cada raíz sigue protegiendo sus propias invariantes; este
 * servicio solo las coordina. Sin estado y sin anotaciones de Spring: se
 * registra como bean desde infraestructura.
 */
public class ServicioDePublicacion {

    private final LoteRepositorio loteRepositorio;
    private final TransformacionRepositorio transformacionRepositorio;

    public ServicioDePublicacion(LoteRepositorio loteRepositorio,
                                 TransformacionRepositorio transformacionRepositorio) {
        this.loteRepositorio = loteRepositorio;
        this.transformacionRepositorio = transformacionRepositorio;
    }

    /**
     * Crea la Presentación (que valida sus propias reglas) y descuenta la
     * cantidad publicada de su Origen. Si el Origen no existe o no alcanza, se
     * lanza la excepción antes de que la Presentación se guarde.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public Presentacion publicar(String id, String vendedorId, String loteId, String transformacionId,
                                 String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                 Cantidad cantidadDisponible, Galeria galeria, PerfilTueste perfilDeTueste,
                                 RolVendedor rolVendedor, FechaDeTueste fechaTueste) {
        Presentacion presentacion = Presentacion.publicar(id, vendedorId, loteId, transformacionId, titulo,
                tipoPresentacion, precio, cantidadDisponible, galeria, perfilDeTueste, rolVendedor, fechaTueste,
                null, null);

        if (!(presentacion instanceof PresentacionTrazable trazable)) {
            throw new ReglaDominioException(
                    "Este servicio publica Presentaciones Trazables; el merchandising tiene su propio caso de uso");
        }
        OrigenDePresentacion origen = trazable.getOrigen();
        if (origen.loteId() != null) {
            descontarDelLote(new CodigoDeLote(origen.loteId()), cantidadDisponible);
        } else {
            descontarDeLaTransformacion(origen.transformacionId(), cantidadDisponible, perfilDeTueste);
        }
        return presentacion;
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
            throw new ReglaDominioException("El Perfil de Tueste publicado (" + perfil
                    + ") no coincide con el de la Transformación (" + transformacion.getPerfilTuesteResultante() + ")");
        }
        transformacion.asignarCantidad(cantidad);
        transformacionRepositorio.guardar(transformacion);
    }
}
