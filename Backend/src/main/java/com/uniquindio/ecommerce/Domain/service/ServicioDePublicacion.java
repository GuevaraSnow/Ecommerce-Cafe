package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;

public class ServicioDePublicacion {

    private final DescuentoDeOrigen descuentoDeOrigen;
    private final UsuarioRepositorio usuarioRepositorio;

    public ServicioDePublicacion(LoteRepositorio loteRepositorio,
                                 TransformacionRepositorio transformacionRepositorio,
                                 UsuarioRepositorio usuarioRepositorio) {
        this.descuentoDeOrigen = new DescuentoDeOrigen(loteRepositorio, transformacionRepositorio);
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Presentacion publicar(String id, String vendedorId, String loteId, String transformacionId,
                                 String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                 Cantidad cantidadDisponible, Galeria galeria, PerfilTueste perfilDeTueste,
                                 FechaDeTueste fechaTueste) {

        var usuario = usuarioRepositorio.buscarPorId(vendedorId)
                .orElseThrow();

        Presentacion presentacion = Presentacion.publicar(id, vendedorId, loteId, transformacionId, titulo,
                tipoPresentacion, precio, cantidadDisponible, galeria, perfilDeTueste, usuario.getRol(), fechaTueste,
                null, null);

        if (!(presentacion instanceof PresentacionTrazable trazable)) {
            throw new ReglaDominioException(
                    "Este servicio publica Presentaciones Trazables; el merchandising tiene su propio caso de uso");
        }
        descuentoDeOrigen.descontar(trazable.getOrigen(), cantidadDisponible, perfilDeTueste);
        return presentacion;
    }
}