package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tbl_transformaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransformacionEntity {

    @Id
    private String id;

    @Column(name = "lote_codigo", nullable = false)
    private String loteCodigo;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "cantidad_entrada_valor", nullable = false)),
            @AttributeOverride(name = "unidad", column = @Column(name = "cantidad_entrada_unidad", nullable = false))
    })
    private CantidadEmbeddable cantidadEntrada;

    @Column(name = "merma_valor", nullable = false)
    private double mermaValor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "cantidad_resultante_valor", nullable = false)),
            @AttributeOverride(name = "unidad", column = @Column(name = "cantidad_resultante_unidad", nullable = false))
    })
    private CantidadEmbeddable cantidadResultante;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "valor", column = @Column(name = "cantidad_disponible_valor", nullable = false)),
            @AttributeOverride(name = "unidad", column = @Column(name = "cantidad_disponible_unidad", nullable = false))
    })
    private CantidadEmbeddable cantidadResultanteDisponible;

    @Column(name = "tostador_id", nullable = false)
    private String tostadorId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_tueste", nullable = false)
    private PerfilTueste perfilTuesteResultante;
}