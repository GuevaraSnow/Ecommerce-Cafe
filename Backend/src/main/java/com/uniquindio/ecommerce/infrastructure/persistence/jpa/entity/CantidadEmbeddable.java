package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Columnas del Value Object Cantidad. */
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CantidadEmbeddable {

    @Column(name = "cantidad_valor", nullable = false)
    private double valor;

    @Column(name = "cantidad_unidad", nullable = false, length = 10)
    private String unidad;
}
