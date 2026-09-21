package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/**
 * Referencia a un archivo alojado en un servicio externo (ej. Cloudinary, S3).
 * Nunca se guarda el contenido del archivo en la base de datos, solo su URL.
 */
public record ArchivoDigital(String url, TipoDeArchivo tipo, TamanoDeArchivo tamano) {

    public ArchivoDigital {
        if (url == null || url.isBlank()) {
            throw new ReglaDominioException("El Archivo Digital debe tener una url");
        }
        if (tipo == null) {
            throw new ReglaDominioException("El Archivo Digital debe indicar un Tipo de Archivo");
        }
        if (tamano == null) {
            throw new ReglaDominioException("El Archivo Digital debe indicar su Tamaño de Archivo");
        }
    }
}
