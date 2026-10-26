package com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Columnas propias de la Presentación Trazable. El Origen guarda solo el id del Lote o de la
 * Transformación (uno de los dos), sin llave foránea: son otros agregados.
 */
@Entity
@DiscriminatorValue("TRAZABLE")
@Getter @Setter
@NoArgsConstructor
public class PresentacionTrazableEntity extends PresentacionEntity {

    @Column(name = "lote_id", length = 50)
    private String loteId;

    @Column(name = "transformacion_id", length = 50)
    private String transformacionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_tueste", length = 20)
    private PerfilTueste perfilTueste;

    @Column(name = "fecha_tueste")
    private LocalDate fechaTueste;

    /** Los descriptores de la Nota de Cata, en el orden en que se escribieron; vacía si no hay nota. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "presentacion_nota_cata", joinColumns = @JoinColumn(name = "presentacion_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "descriptor", nullable = false, length = 100)
    private List<String> notaDeCata = new ArrayList<>();
}
