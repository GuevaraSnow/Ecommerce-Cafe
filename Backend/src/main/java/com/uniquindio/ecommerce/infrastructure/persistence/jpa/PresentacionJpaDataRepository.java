package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PresentacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

interface PresentacionJpaDataRepository extends JpaRepository<PresentacionEntity, String> {

    /**
     * Presentaciones disponibles para la venta: activas, no dadas de baja y, si son trazables, con
     * fecha de tueste igual o posterior a {@code fechaMinimaDeTueste} (frescas). Los criterios en
     * {@code null} no filtran. Ordenadas por título; el paginado y el conteo los hace la base de datos.
     */
    @Query("""
            select p from PresentacionEntity p
            where p.estado = com.uniquindio.ecommerce.Domain.valueobject.EstadoDePublicacion.ACTIVA
              and p.eliminada = false
              and (type(p) = ArticuloMerchandisingEntity
                   or treat(p as PresentacionTrazableEntity).fechaTueste is null
                   or treat(p as PresentacionTrazableEntity).fechaTueste >= :fechaMinimaDeTueste)
              and (:titulo is null or lower(p.titulo) like lower(concat('%', :titulo, '%')))
              and (:tipo is null or p.tipo = :tipo)
              and (:vendedorId is null or p.vendedorId = :vendedorId)
              and (:precioMinimo is null or p.precio.monto >= :precioMinimo)
              and (:precioMaximo is null or p.precio.monto <= :precioMaximo)
            order by lower(p.titulo), p.id
            """)
    Page<PresentacionEntity> buscarDisponibles(@Param("titulo") String titulo,
                                               @Param("tipo") TipoDePresentacion tipo,
                                               @Param("vendedorId") String vendedorId,
                                               @Param("precioMinimo") Double precioMinimo,
                                               @Param("precioMaximo") Double precioMaximo,
                                               @Param("fechaMinimaDeTueste") LocalDate fechaMinimaDeTueste,
                                               Pageable pageable);
}
