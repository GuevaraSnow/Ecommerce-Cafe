package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.ResenaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ResenaJpaDataRepository extends JpaRepository<ResenaEntity, String> {

    List<ResenaEntity> findByPresentacionIdOrderByFechaDesc(String presentacionId);

    boolean existsByCompradorIdAndPresentacionId(String compradorId, String presentacionId);
}
