package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.LoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Consultas derivadas sobre Lote.
 */
interface LoteJpaDataRepository extends JpaRepository<LoteEntity, String> {

    Optional<LoteEntity> findFirstByCodigoDeLoteStartingWithOrderByCodigoDeLoteDesc(String prefijo);

    /** Un Lote por su código, sin los dados de baja. */
    Optional<LoteEntity> findByCodigoDeLoteAndEliminadoLogicamenteFalse(String codigoDeLote);

}
