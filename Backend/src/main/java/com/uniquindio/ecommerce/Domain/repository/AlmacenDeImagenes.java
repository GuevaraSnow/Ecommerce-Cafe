package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public interface AlmacenDeImagenes {

    /**
     * Guarda la imagen en un almacén externo y devuelve la URL pública con la que se muestra.
     *
     * @throws ReglaDominioException si el almacén no responde o rechaza la imagen
     */
    String guardar(byte[] contenido, String nombreOriginal, String tipo);
}
