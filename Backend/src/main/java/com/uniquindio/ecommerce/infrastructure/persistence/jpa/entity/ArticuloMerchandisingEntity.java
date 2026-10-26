package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Columnas propias del Artículo de Merchandising: no tiene Origen ni datos de tueste. */
@Entity
@DiscriminatorValue("MERCHANDISING")
@Getter @Setter
@NoArgsConstructor
public class ArticuloMerchandisingEntity extends PresentacionEntity {

    @Column(length = 100)
    private String material;

    @Column(length = 500)
    private String descripcion;
}
