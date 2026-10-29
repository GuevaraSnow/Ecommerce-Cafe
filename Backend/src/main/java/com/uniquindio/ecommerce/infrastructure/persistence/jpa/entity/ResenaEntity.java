package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Tabla resenas: la opinión de un Comprador sobre una Presentación y la respuesta de su vendedor. */
@Entity
@Table(name = "resenas")
@Getter @Setter
@NoArgsConstructor
public class ResenaEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "comprador_id", nullable = false, length = 50)
    private String compradorId;

    @Column(name = "presentacion_id", nullable = false, length = 50)
    private String presentacionId;

    @Column(name = "vendedor_id", nullable = false, length = 50)
    private String vendedorId;

    @Column(nullable = false)
    private int calificacion;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Embedded
    private RespuestaAResenaEmbeddable respuesta;
}
