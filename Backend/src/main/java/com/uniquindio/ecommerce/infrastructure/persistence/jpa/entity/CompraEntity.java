package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla compras. Separada del dominio: solo la usa el adaptador JPA a través de
 * CompraPersistenceMapper. Los detalles van en una tabla aparte (detalles_compra).
 */
@Entity
@Table(name = "compras")
@Getter @Setter
@NoArgsConstructor
public class CompraEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "comprador_id", nullable = false, length = 50)
    private String compradorId;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDeCompra estado;

    @Embedded
    private DireccionDeEnvioEmbeddable direccionEnvio;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "detalles_compra", joinColumns = @JoinColumn(name = "compra_id"))
    private List<DetalleDeCompraEmbeddable> detalles = new ArrayList<>();
}
