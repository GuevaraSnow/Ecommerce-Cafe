package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.repository.PresentacionVendida;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PresentacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface PresentacionJpaDataRepository extends JpaRepository<PresentacionEntity, String> {

    /** Una Presentación por su id, sin las dadas de baja. */
    Optional<PresentacionEntity> findByIdAndEliminadaFalse(String id);

    /**
     * Presentaciones disponibles para la venta: activas, no dadas de baja y, si son trazables, con
     * fecha de tueste igual o posterior a {@code fechaMinimaDeTueste} (frescas). Los criterios en
     * {@code null} no filtran. {@code calificacionMinima} se compara con el promedio de las Reseñas de la
     * Presentación (calculado en la base): sin Reseñas no hay promedio y la Presentación no cumple.
     * Ordenadas por título; el paginado y el conteo los hace la base de datos.
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
              and (:calificacionMinima is null
                   or (select avg(r.calificacion) from ResenaEntity r where r.presentacionId = p.id) >= :calificacionMinima)
            order by lower(p.titulo), p.id
            """)
    Page<PresentacionEntity> buscarDisponibles(@Param("titulo") String titulo,
                                               @Param("tipo") TipoDePresentacion tipo,
                                               @Param("vendedorId") String vendedorId,
                                               @Param("precioMinimo") Double precioMinimo,
                                               @Param("precioMaximo") Double precioMaximo,
                                               @Param("calificacionMinima") Double calificacionMinima,
                                               @Param("fechaMinimaDeTueste") LocalDate fechaMinimaDeTueste,
                                               Pageable pageable);

    /**
     * El ranking de ventas: une cada Compra con los detalles que tiene y con la Presentación de cada
     * detalle, agrupa por Presentación y cuenta las Compras distintas (una Compra con la misma Presentación
     * dos veces cuenta una). Los ingresos son precio congelado por cantidad. Solo cuentan las Compras en
     * {@code estados}; la paginación solo limita cuántas filas se traen.
     */
    @Query("""
            select new com.uniquindio.ecommerce.Domain.repository.PresentacionVendida(
                       p.id, p.titulo, count(distinct c.id), sum(d.precioCongelado.monto * d.cantidad.valor))
            from CompraEntity c join c.detalles d, PresentacionEntity p
            where p.id = d.presentacionId and c.estado in :estados
            group by p.id, p.titulo
            order by count(distinct c.id) desc, sum(d.precioCongelado.monto * d.cantidad.valor) desc, p.titulo
            """)
    List<PresentacionVendida> masVendidas(@Param("estados") Collection<EstadoDeCompra> estados, Pageable pageable);
}
