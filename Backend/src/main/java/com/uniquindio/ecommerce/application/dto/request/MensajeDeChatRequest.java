package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Mapea a: MensajeDeChat. Un mensaje anterior de la conversación con el asesor.
public record MensajeDeChatRequest(
        @NotBlank(message = "El rol del mensaje es obligatorio")
        @Pattern(regexp = "usuario|asistente", message = "El rol debe ser usuario o asistente")
        String rol,

        @NotBlank(message = "El texto del mensaje es obligatorio")
        @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
        String texto
) {}
