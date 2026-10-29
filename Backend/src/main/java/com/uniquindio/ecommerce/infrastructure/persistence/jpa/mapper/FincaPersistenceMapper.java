package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.FincaEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.UbicacionEmbeddable;
import org.springframework.stereotype.Component;

@Component
public class FincaPersistenceMapper {

    public FincaEntity toEntity(Finca domain) {
        if (domain == null) return null;

        UbicacionEmbeddable ubicacion = new UbicacionEmbeddable(
                domain.getUbicacion().departamento(),
                domain.getUbicacion().municipio()
        );

        return new FincaEntity(
                domain.getId(),
                domain.getPropietarioId(),
                domain.getNombre(),
                ubicacion,
                domain.getAltitud().metrosSobreNivelMar(),
                domain.isEliminada()
        );
    }

    public Finca toDomain(FincaEntity entity) {
        if (entity == null) return null;

        Ubicacion ubicacion = new Ubicacion(
                entity.getUbicacion().getDepartamento(),
                entity.getUbicacion().getMunicipio()
        );

        return Finca.reconstruir(
                entity.getId(),
                entity.getPropietarioId(),
                entity.getNombre(),
                ubicacion,
                new Altitud(entity.getAltitudMetros()),
                entity.isEliminadaLogicamente()
        );
    }
}