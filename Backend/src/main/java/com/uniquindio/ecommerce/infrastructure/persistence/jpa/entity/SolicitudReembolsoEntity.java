package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeReembolso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Tabla solicitudes_reembolso: una fila por Presentación de una Compra que se pide reembolsar. */
@Entity
@Table(name = "solicitudes_reembolso")
@Getter @Setter
@NoArgsConstructor
public class SolicitudReembolsoEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "compra_id", nullable = false, length = 36)
    private String compraId;

    @Column(name = "presentacion_id", nullable = false, length = 50)
    private String presentacionId;

    @Column(name = "vendedor_id", nullable = false, length = 50)
    private String vendedorId;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDeReembolso estado;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
}
