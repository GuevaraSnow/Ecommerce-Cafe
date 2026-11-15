package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.valueobject.MensajeDeChat;

import java.util.List;

public interface AsistenteIA {

    /**
     * Pide a la IA una respuesta para el mensaje, según las instrucciones y el historial de la conversación.
     * Lanza ReglaDominioException si el servicio de IA no responde.
     */
    String responder(String instrucciones, List<MensajeDeChat> historial, String mensaje);
}
