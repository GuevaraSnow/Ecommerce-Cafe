package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.TransformacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface TransformacionJpaDataRepository extends JpaRepository<TransformacionEntity, String> {
}