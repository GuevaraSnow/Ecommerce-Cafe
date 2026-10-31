package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PresentacionEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.PresentacionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Adaptador de PresentacionRepositorio sobre Spring Data JPA (H2 por ahora). */
@Repository
@RequiredArgsConstructor
public class PresentacionRepositorioJpa implements PresentacionRepositorio {

    private final PresentacionJpaDataRepository dataRepository;
    private final PresentacionPersistenceMapper mapper;

    @Override
    public Optional<Presentacion> buscarPorId(String id) {
        return dataRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void guardar(Presentacion presentacion) {
        dataRepository.save(mapper.toEntity(presentacion));
    }

    @Override
    public Pagina<Presentacion> buscarDisponibles(FiltroDePresentaciones filtro, int pagina, int tamano) {
        Page<PresentacionEntity> resultado = dataRepository.buscarDisponibles(filtro.titulo(), filtro.tipo(),
                filtro.vendedorId(), filtro.precioMinimo(), filtro.precioMaximo(), filtro.calificacionMinima(),
                fechaMinimaDeTueste(), PageRequest.of(pagina, tamano));
        return new Pagina<>(resultado.getContent().stream().map(mapper::toDomain).toList(),
                pagina, tamano, resultado.getTotalElements());
    }

    @Override
    public List<Presentacion> sugerirDisponiblesPorTitulo(String texto, int limite) {
        return dataRepository.buscarDisponibles(texto, null, null, null, null, null, fechaMinimaDeTueste(),
                        PageRequest.of(0, limite))
                .getContent().stream().map(mapper::toDomain).toList();
    }

    /** Un café tostado antes de esta fecha ya no está fresco (más de 30 días): la Presentación está vencida. */
    private LocalDate fechaMinimaDeTueste() {
        return LocalDate.now().minusDays(PresentacionTrazable.DIAS_MAXIMOS_FRESCURA);
    }
}
