package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.ResenaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface ResenaJpaDataRepository extends JpaRepository<ResenaEntity, String> {

    boolean existsByCompradorIdAndPresentacionId(String compradorId, String presentacionId);
}
