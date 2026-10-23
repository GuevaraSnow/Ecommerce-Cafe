package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarContrasenaRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato de email no es válido")
        String email
) {}