package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.util.List;

/**
 * Conjunto de imágenes de una Presentación: entre 1 y 10, con
 * exactamente una marcada como principal (invariante 6).
 */
public record Galeria(List<ImagenDePresentacion> imagenes) {

    public Galeria {
        if (imagenes == null || imagenes.isEmpty() || imagenes.size() > 10) {
            throw new ReglaDominioException("La Galería debe tener entre 1 y 10 imágenes");
        }
        long principales = imagenes.stream().filter(ImagenDePresentacion::principal).count();
        if (principales != 1) {
            throw new ReglaDominioException("La Galería debe tener exactamente una imagen principal");
        }
        imagenes = List.copyOf(imagenes);
    }
}
