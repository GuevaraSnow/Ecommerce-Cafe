package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una fila de la tabla detalles_compra: el Value Object DetalleDeCompra (Presentación por id,
 * precio congelado y cantidad). No tiene id propio: pertenece a su Compra.
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DetalleDeCompraEmbeddable {

    @Column(name = "presentacion_id", nullable = false, length = 50)
    private String presentacionId;

    @Embedded
    private PrecioEmbeddable precioCongelado;

    @Embedded
    private CantidadEmbeddable cantidad;
}
