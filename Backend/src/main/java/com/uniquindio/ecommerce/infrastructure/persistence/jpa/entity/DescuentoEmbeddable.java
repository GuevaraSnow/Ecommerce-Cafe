package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Columnas del Value Object Descuento; las tres en nulo cuando la Presentación no tiene descuento. */
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DescuentoEmbeddable {

    @Column(name = "descuento_porcentaje")
    private Integer porcentaje;

    @Column(name = "descuento_fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "descuento_fecha_fin")
    private LocalDate fechaFin;
}
