package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.valueobject.MensajeDeChat;
import com.uniquindio.ecommerce.application.dto.request.MensajeDeChatRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/** Traduce el historial del DTO al dominio; un historial ausente cuenta como vacío. */
@Component
public class AsistenteMapper {

    public List<MensajeDeChat> toHistorial(List<MensajeDeChatRequest> historial) {
        if (historial == null) {
            return List.of();
        }
        return historial.stream()
                .map(mensaje -> new MensajeDeChat(mensaje.rol(), mensaje.texto()))
                .toList();
    }
}
