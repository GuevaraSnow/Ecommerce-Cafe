package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.NotificadorDeCorreo;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import org.springframework.stereotype.Component;

@Component
public class NotificadorCorreoMemoria implements NotificadorDeCorreo {

    @Override
    public void enviarCodigoDeRecuperacion(Email email, String codigo) {
        System.out.println("Enviando código de recuperación " + codigo + " a: " + email.email());
    }

    @Override
    public void enviarConfirmacionDeCompra(Email email, Compra compra) {
        System.out.println("Enviando confirmación de la compra " + compra.getId() + " ("
                + compra.getDetalles().size() + " Presentaciones) a: " + email.email());
    }
}