package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Mapea a: PublicarArticuloMerchandising.ejecutar(...)
// Sin origen ni datos de tueste: un artículo de merchandising no los tiene.
// No incluye el id (lo genera el servidor) ni el vendedor y su rol (salen del usuario autenticado).
public record PublicarArticuloMerchandisingRequest(
        @NotBlank(message = "El título es obligatorio")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotBlank(message = "El material es obligatorio")
        String material,

        @NotNull(message = "El precio es obligatorio")
        @Valid
        PrecioRequest precio,

        @NotNull(message = "La cantidad es obligatoria")
        @Valid
        CantidadRequest cantidad,

        @NotNull(message = "La galería es obligatoria")
        List<@NotNull(message = "La galería no admite imágenes nulas") @Valid ImagenRequest> galeria
) {}
