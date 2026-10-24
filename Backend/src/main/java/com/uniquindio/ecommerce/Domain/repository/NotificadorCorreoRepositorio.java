package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.valueobject.Email;

public interface NotificadorCorreoRepositorio {
    void enviarConfirmacionDeCompra(Email destinatario, String compraId, double total);
    void enviarCodigoDeRecuperacion(Email destinatario, String codigo);
}