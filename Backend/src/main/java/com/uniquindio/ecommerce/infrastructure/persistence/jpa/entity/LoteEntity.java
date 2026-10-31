package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.Domain.valueobject.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_lotes")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class LoteEntity {

    @Id
    @Column(name = "codigo", length = 12)
    private String codigoDeLote;

    @Column(name = "finca_id", nullable = false)
    private String fincaId;

    @Embedded
    private CosechaEmbeddable cosecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "proceso_beneficio", length = 20, nullable = false)
    private ProcesoDeBeneficio procesoDeBeneficio;

    @Column(name = "variedad", length = 50, nullable = false)
    private String variedad;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoDelCafe estado;

    @Embedded
    private CantidadEmbeddable cantidadDisponible;

    @Column(nullable = false)
    private boolean eliminadoLogicamente;

}
