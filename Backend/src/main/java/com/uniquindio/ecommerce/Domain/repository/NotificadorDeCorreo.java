package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

public interface NotificadorDeCorreo {
    void enviarCodigoDeRecuperacion(Email email, String codigo);

    /** Avisa al Comprador que su Compra quedó confirmada (con sus Presentaciones y la dirección de envío). */
    void enviarConfirmacionDeCompra(Email email, Compra compra);
}