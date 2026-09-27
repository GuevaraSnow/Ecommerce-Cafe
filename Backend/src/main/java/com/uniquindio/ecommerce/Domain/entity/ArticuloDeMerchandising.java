package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
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
    private final String material;
    private final String descripcion;

    private ArticuloDeMerchandising(String id, String vendedorId, String titulo, TipoDePresentacion tipoPresentacion,
                             Precio precio, Cantidad cantidadDisponible, Galeria galeria, String material, String descripcion) {
        super(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible, galeria);
        this.material = material;
        this.descripcion = descripcion;
    }

    /**
     * Valida y crea un Artículo de Merchandising (exención 7-B: sin Origen,
     * Perfil de Tueste ni Fecha de Tueste).
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    static ArticuloDeMerchandising publicar(String id, String vendedorId, String titulo,
                                             TipoDePresentacion tipoPresentacion, Precio precio,
                                             Cantidad cantidadDisponible, Galeria galeria, String material,
                                             String descripcion) {
        if (material == null || material.isBlank()) {
            throw new ReglaDominioException("El Artículo de Merchandising debe especificar un material");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaDominioException("El Artículo de Merchandising debe tener una descripción");
        }
        return new ArticuloDeMerchandising(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible,
                galeria, material, descripcion);
    }

    public String getMaterial() {
        return material;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
