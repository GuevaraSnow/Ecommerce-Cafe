package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

// Mapea a: PublicarPresentacion.ejecutar(...) (Presentación Trazable)
// No incluye el id (lo genera el servidor) ni el vendedor y su rol (salen del usuario autenticado).
// Solo valida la forma; que el origen sea Lote o Transformación (exactamente uno), la
// frescura, el perfil de tueste según el rol y la galería válida los aplica el dominio.
public record PublicarPresentacionRequest(
        @NotBlank(message = "El título es obligatorio")
        String titulo,

        @NotNull(message = "El tipo de presentación es obligatorio")
        TipoDePresentacion tipo,

        @NotNull(message = "El precio es obligatorio")
        @Valid
        PrecioRequest precio,

        @NotNull(message = "La cantidad es obligatoria")
        @Valid
        CantidadRequest cantidad,

        @NotNull(message = "La galería es obligatoria")
        List<@NotNull(message = "La galería no admite imágenes nulas") @Valid ImagenRequest> galeria,

        String loteId,

        String transformacionId,

        PerfilTueste perfilTueste,

        LocalDate fechaTueste
) {}
