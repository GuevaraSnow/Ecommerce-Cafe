package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record MensajeDeChat(String rol, String texto) {

    public MensajeDeChat {

        if (rol == null || !(rol.equals("usuario") || rol.equals("asistente"))) {
            throw new ReglaDominioException("Ingrese un rol valido");
        }

        if (texto == null || texto.isBlank()) {
            throw new ReglaDominioException("Escriba su mensaje");
        }

        if(texto.length() > 500){
            throw new ReglaDominioException("El mensaje no puede superar 500 caracteres");
        }
    }
}
