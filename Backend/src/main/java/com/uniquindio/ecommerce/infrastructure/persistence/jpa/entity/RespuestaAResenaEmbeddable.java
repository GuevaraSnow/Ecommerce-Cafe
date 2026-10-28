package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Columnas del Value Object RespuestaAResena. Son opcionales: quedan vacías mientras el
 * vendedor no haya respondido.
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RespuestaAResenaEmbeddable {

    @Column(name = "respuesta_texto", length = 500)
    private String texto;

    @Column(name = "respuesta_fecha")
    private LocalDateTime fecha;
}
