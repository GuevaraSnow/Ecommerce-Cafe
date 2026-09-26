package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.Galeria;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;

/**
 * Presentación no alimenticia (manillas, decoración, etc.). No tiene Origen,
 * Perfil de Tueste ni Fecha de Tueste, así que no puede violar esas reglas
 * (exención 7-B). Se crea únicamente desde {@link Presentacion#publicar}.
 */
public class ArticuloDeMerchandising extends Presentacion {

    ArticuloDeMerchandising(String id, String vendedorId, String titulo, TipoDePresentacion tipoPresentacion,
                             Precio precio, Cantidad cantidadDisponible, Galeria galeria) {
        super(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible, galeria);
    }
}
