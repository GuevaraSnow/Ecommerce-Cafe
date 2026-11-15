package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.AlmacenDeImagenes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Caso de uso: SubirImagen.
 * (Actor: Caficultor, Tostador o Vendedor de derivados) — valida que el archivo sea una imagen
 * permitida y la guarda en el almacén externo; devuelve la URL para ponerla en la galería de una Presentación.
 */
@Service
@RequiredArgsConstructor
public class SubirImagen {

    private static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final AlmacenDeImagenes almacen;

    public String ejecutar(byte[] contenido, String nombreOriginal, String tipo) {
        if (contenido == null || contenido.length == 0) {
            throw new ReglaDominioException("Seleccione una imagen");
        }
        if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
            throw new ReglaDominioException("La imagen debe ser JPG, PNG o WebP");
        }
        if (contenido.length > MAX_BYTES) {
            throw new ReglaDominioException("La imagen no puede superar 2 MB");
        }
        return almacen.guardar(contenido, nombreOriginal, tipo);
    }
}
