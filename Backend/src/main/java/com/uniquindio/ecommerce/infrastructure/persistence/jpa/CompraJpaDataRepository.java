package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * Consultas derivadas sobre compras. "Detalles_PresentacionId" navega dentro de la tabla
 * detalles_compra: busca compras que tengan un detalle con esa Presentación.
 */
interface CompraJpaDataRepository extends JpaRepository<CompraEntity, String> {

    boolean existsByEstadoInAndDetalles_PresentacionId(Collection<EstadoDeCompra> estados, String presentacionId);

    boolean existsByCompradorIdAndEstadoAndDetalles_PresentacionId(
            String compradorId, EstadoDeCompra estado, String presentacionId);
}
