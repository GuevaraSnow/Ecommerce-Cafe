package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
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

    private final DescuentoDeOrigen descuentoDeOrigen;
    private final UsuarioRepositorio usuarioRepositorio;

    public ServicioDePublicacion(LoteRepositorio loteRepositorio,
                                 TransformacionRepositorio transformacionRepositorio, UsuarioRepositorio usuarioRepositorio) {
        this.descuentoDeOrigen = new DescuentoDeOrigen(loteRepositorio, transformacionRepositorio);
        this.usuarioRepositorio = usuarioRepositorio;  {
        }
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
                                 Rol rolVendedor, FechaDeTueste fechaTueste) {

        var usuario = usuarioRepositorio.buscarPorId(vendedorId)
                .orElseThrow();

        if (usuario.getRol() == Rol.COMPRADOR) {
            throw new ReglaDominioException("Un usuario con rol COMPRADOR no está autorizado para publicar presentaciones.");
        }

        Presentacion presentacion = Presentacion.publicar(id, vendedorId, loteId, transformacionId, titulo,
                tipoPresentacion, precio, cantidadDisponible, galeria, perfilDeTueste, rolVendedor, fechaTueste,
                null, null);

        if (!(presentacion instanceof PresentacionTrazable trazable)) {
            throw new ReglaDominioException(
                    "Este servicio publica Presentaciones Trazables; el merchandising tiene su propio caso de uso");
        }
        descuentoDeOrigen.descontar(trazable.getOrigen(), cantidadDisponible, perfilDeTueste);
        return presentacion;
    }
}
