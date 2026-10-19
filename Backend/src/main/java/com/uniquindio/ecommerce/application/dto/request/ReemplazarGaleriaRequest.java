package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Mapea a: ReemplazarGaleriaDePresentacion.ejecutar(...)
// Solo valida la forma; que haya entre 1 y 10 imágenes con exactamente una principal lo aplica el dominio.
public record ReemplazarGaleriaRequest(
        @NotNull(message = "La galería es obligatoria")
        List<@NotNull(message = "La galería no admite imágenes nulas") @Valid ImagenRequest> galeria
) {}
