package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SoliictarReembolsoRequest(
        @NotBlank(message = "El motivo es obligatorio")
        @Size(min=10,max =500)
        String motivo
) {
}
