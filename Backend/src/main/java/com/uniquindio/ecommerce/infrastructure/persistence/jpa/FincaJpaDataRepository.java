package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.FincaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface FincaJpaDataRepository extends JpaRepository<FincaEntity, String> {
}