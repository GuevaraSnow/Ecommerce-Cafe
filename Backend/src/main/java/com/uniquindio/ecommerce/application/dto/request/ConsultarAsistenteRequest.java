package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// Es el cuerpo de POST /api/asistente; el historial es opcional (la conversación hasta ahora).
public record ConsultarAsistenteRequest(
        @NotBlank(message = "Escriba su mensaje")
        @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
        String mensaje,

        @Valid
        @Size(max = 20, message = "El historial no puede tener más de 20 mensajes")
        List<MensajeDeChatRequest> historial
) {}
