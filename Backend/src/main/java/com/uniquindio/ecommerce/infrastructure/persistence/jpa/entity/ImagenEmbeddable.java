package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Una imagen de la Galería; vive en la tabla presentacion_galeria, una fila por imagen. */
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ImagenEmbeddable {

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private boolean principal;
}
