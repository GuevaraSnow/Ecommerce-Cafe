package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.application.dto.response.ImagenSubidaResponse;
import com.uniquindio.ecommerce.application.usecase.SubirImagen;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Subida de imágenes para las galerías: solo quienes publican Presentaciones. */
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
@Tag(name = "Imágenes", description = "Subida de imágenes para la galería de las Presentaciones")
public class ImagenController {

    private static final String PUBLICAR = "hasAnyAuthority('CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS')";

    private final SubirImagen subirImagen;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(PUBLICAR)
    @Operation(summary = "Subir una imagen",
            description = "JPG, PNG o WebP de hasta 2 MB. Devuelve la URL para ponerla en la galería de una Presentación.")
    @ApiResponse(responseCode = "422", description = "Archivo inválido o almacén de imágenes no disponible")
    public ImagenSubidaResponse subir(@RequestParam("archivo") MultipartFile archivo) {
        try {
            return new ImagenSubidaResponse(
                    subirImagen.ejecutar(archivo.getBytes(), archivo.getOriginalFilename(), archivo.getContentType()));
        } catch (IOException e) {
            throw new ReglaDominioException("No se pudo leer la imagen");
        }
    }
}
