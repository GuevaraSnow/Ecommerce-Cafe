package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_fincas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FincaEntity {

    @Id
    private String id;

    @Column(name = "propietario_id", nullable = false)
    private String propietarioId;

    @Column(nullable = false)
    private String nombre;

    @Embedded
    private UbicacionEmbeddable ubicacion;

    @Column(name = "altitud_metros", nullable = false)
    private double altitudMetros;

    @Column(name = "eliminada_logicamente", nullable = false)
    private boolean eliminadaLogicamente;
}