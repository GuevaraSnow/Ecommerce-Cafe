package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;

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

    private ArticuloDeMerchandising(String id, String vendedorId, String titulo, TipoDePresentacion tipoPresentacion,
                                    Precio precio, Cantidad cantidadDisponible, Galeria galeria,
                                    EstadoDePublicacion estado, boolean eliminada, Descuento descuento,
                                    String material, String descripcion) {
        super(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible, galeria,
                estado, eliminada, descuento);
        this.material = material;
        this.descripcion = descripcion;
    }

    /** Revive un Artículo de Merchandising ya guardado, tal como quedó. No valida: solo la usa la persistencia. */
    public static ArticuloDeMerchandising reconstruir(String id, String vendedorId, String titulo,
                                                      TipoDePresentacion tipoPresentacion, Precio precio,
                                                      Cantidad cantidadDisponible, Galeria galeria,
                                                      EstadoDePublicacion estado, boolean eliminada,
                                                      Descuento descuento, String material, String descripcion) {
        return new ArticuloDeMerchandising(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible,
                galeria, estado, eliminada, descuento, material, descripcion);
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
        if (!"unidad".equals(cantidadDisponible.unidad())) {
            throw new ReglaDominioException(
                    "Un Artículo de Merchandising se cuenta en 'unidad', no en " + cantidadDisponible.unidad());
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