package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.Domain.valueobject.TemporadaDeCosecha;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CosechaEmbeddable {

    @Column(name = "cosecha_anio", nullable = false)
    private int anio;

    @Enumerated(EnumType.STRING)
    @Column(name = "cosecha_temporada", nullable = false)
    private TemporadaDeCosecha temporada;
}
