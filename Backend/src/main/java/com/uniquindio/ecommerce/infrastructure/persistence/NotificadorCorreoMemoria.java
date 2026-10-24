package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.repository.NotificadorCorreoRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import org.springframework.stereotype.Repository;

@Repository
public class NotificadorCorreoMemoria implements NotificadorCorreoRepositorio {

    @Override
    public void enviarConfirmacionDeCompra(Email destinatario, String compraId, double total) {
        System.out.printf("[CORREO SIMULADO] Confirmación de compra #%s enviada a %s. Total: $%.2f%n",
                compraId, destinatario.email(), total);
    }

    @Override
    public void enviarCodigoDeRecuperacion(Email destinatario, String codigo) {
        System.out.printf("[CORREO SIMULADO] Código de recuperación enviado a %s: %s (Válido por 15 min)%n",
                destinatario.email(), codigo);
    }
}