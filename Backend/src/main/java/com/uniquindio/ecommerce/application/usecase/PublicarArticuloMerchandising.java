package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.Galeria;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.RolVendedor;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;

/**
 * Caso de uso: PublicarArticuloMerchandising.
 * (Actor: Vendedor de Derivados) — publica un Artículo de Merchandising no
 * alimenticio, sin Origen ni Ficha de Origen (regla 7-B).
 */
public class PublicarArticuloMerchandising {

    private final PresentacionRepositorio repositorio;

    public PublicarArticuloMerchandising(PresentacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public ArticuloDeMerchandising ejecutar(String id, String vendedorId, String titulo, Precio precio,
                                             Cantidad cantidadDisponible, Galeria galeria, String material,
                                             String descripcion, RolVendedor rolVendedor) {
        Presentacion presentacion = Presentacion.publicar(id, vendedorId, null, null, titulo,
                TipoDePresentacion.MERCHANDISING, precio, cantidadDisponible, galeria, null, rolVendedor, null,
                material, descripcion);
        repositorio.guardar(presentacion);
        return (ArticuloDeMerchandising) presentacion;
    }
}
