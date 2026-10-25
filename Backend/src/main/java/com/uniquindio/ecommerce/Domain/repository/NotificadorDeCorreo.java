package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.valueobject.Email;

public interface NotificadorDeCorreo {
    void enviarCodigoDeRecuperacion(Email email, String codigo);
}