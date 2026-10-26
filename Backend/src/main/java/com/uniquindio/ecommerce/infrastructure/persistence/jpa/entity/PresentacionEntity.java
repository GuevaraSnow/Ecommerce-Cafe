package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDePublicacion;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla presentaciones. Una sola tabla para las dos variantes (SINGLE_TABLE): la columna
 * "clase" dice si la fila es una Presentación Trazable o un Artículo de Merchandising, y las
 * columnas propias de cada una quedan en nulo en la otra. Separada del dominio: solo la usa el
 * adaptador JPA a través de PresentacionPersistenceMapper.
 */
@Entity
@Table(name = "presentaciones")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "clase", length = 20)
@Getter @Setter
@NoArgsConstructor
public abstract class PresentacionEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "vendedor_id", nullable = false, length = 50)
    private String vendedorId;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoDePresentacion tipo;

    @Embedded
    private PrecioEmbeddable precio;

    @Embedded
    private CantidadEmbeddable cantidadDisponible;

    @Embedded
    private DescuentoEmbeddable descuento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDePublicacion estado;

    @Column(nullable = false)
    private boolean eliminada;

    /** La posición conserva el orden de la Galería. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "presentacion_galeria", joinColumns = @JoinColumn(name = "presentacion_id"))
    @OrderColumn(name = "posicion")
    private List<ImagenEmbeddable> galeria = new ArrayList<>();
}
