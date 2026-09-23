package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RealizarCompraRequest(
    @NotBlank(message = "El modelo es obligatorio")
    String presentacionId,

    @NotBlank(message = "El comprador es obligatorio")
    String compradorId
    )
{}
