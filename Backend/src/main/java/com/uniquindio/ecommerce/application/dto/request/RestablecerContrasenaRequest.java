package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RestablecerContrasenaRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato de email no es válido")
        String email,

        @NotBlank(message = "El código de recuperación es obligatorio")
        String codigoRecuperacion,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        String nuevaContrasena
) {}