package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Mapea a: SolicitudReembolso. Es el cuerpo de POST /api/reembolsos; el Comprador sale del token.
public record SolicitarReembolsoRequest(
        @NotBlank(message = "La Compra a reembolsar es obligatoria")
        String compraId,

        @NotBlank(message = "La Presentacion a reembolsar es obligatoria")
        String presentacionId,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
        String motivo
) {}
