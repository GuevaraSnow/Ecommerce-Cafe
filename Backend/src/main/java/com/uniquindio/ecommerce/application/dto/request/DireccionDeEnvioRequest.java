package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Mapea a: DireccionDeEnvio. Es el cuerpo de PUT /api/compras/{id}/confirmar.
public record DireccionDeEnvioRequest(
        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 80, message = "La ciudad no puede superar 80 caracteres")
        String ciudad,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 150, message = "La direccion no puede superar 150 caracteres")
        String direccion,

        @NotBlank(message = "El destinatario es obligatorio")
        @Size(max = 100, message = "El destinatario no puede superar 100 caracteres")
        String destinatario
) {}
