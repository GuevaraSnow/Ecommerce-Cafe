package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.repository.NotificadorDeCorreo;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import org.springframework.stereotype.Component;

@Component
public class NotificadorCorreoMemoria implements NotificadorDeCorreo {

    @Override
    public void enviarCodigoDeRecuperacion(Email email, String codigo) {
        System.out.println("Enviando código de recuperación " + codigo + " a: " + email.email());
    }
}