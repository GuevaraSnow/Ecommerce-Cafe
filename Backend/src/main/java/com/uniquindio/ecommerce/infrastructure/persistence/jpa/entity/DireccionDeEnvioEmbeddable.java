package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Columnas del Value Object DireccionDeEnvio. Son opcionales porque un carrito (Compra
 * PENDIENTE) todavía no tiene dirección: se fija al confirmar.
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DireccionDeEnvioEmbeddable {

    @Column(name = "envio_ciudad", length = 80)
    private String ciudad;

    @Column(name = "envio_direccion", length = 150)
    private String direccion;

    @Column(name = "envio_destinatario", length = 100)
    private String destinatario;
}
