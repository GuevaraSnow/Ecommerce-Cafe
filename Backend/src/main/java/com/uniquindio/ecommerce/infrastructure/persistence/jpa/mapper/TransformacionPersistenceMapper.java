package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CantidadEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.TransformacionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransformacionPersistenceMapper {

    public TransformacionEntity toEntity(Transformacion domain) {
        if (domain == null) return null;

        CantidadEmbeddable entrada = new CantidadEmbeddable(
                domain.getCantidadEntrada().valor(),
                domain.getCantidadEntrada().unidad()
        );

        CantidadEmbeddable resultante = new CantidadEmbeddable(
                domain.getCantidadResultante().valor(),
                domain.getCantidadResultante().unidad()
        );

        CantidadEmbeddable disponible = new CantidadEmbeddable(
                domain.getCantidadResultanteDisponible().valor(),
                domain.getCantidadResultanteDisponible().unidad()
        );

        return new TransformacionEntity(
                domain.getId(),
                domain.getLoteCodigo(),
                entrada,
                domain.getMerma().valor(),
                resultante,
                disponible,
                domain.getTostadorId(),
                domain.getFecha(),
                domain.getPerfilTuesteResultante()
        );
    }

    public Transformacion toDomain(TransformacionEntity entity) {
        if (entity == null) return null;

        Cantidad entrada = new Cantidad(entity.getCantidadEntrada().getValor(), entity.getCantidadEntrada().getUnidad());
        Cantidad resultante = new Cantidad(entity.getCantidadResultante().getValor(), entity.getCantidadResultante().getUnidad());
        Cantidad disponible = new Cantidad(entity.getCantidadResultanteDisponible().getValor(), entity.getCantidadResultanteDisponible().getUnidad());

        return Transformacion.reconstruir(
                entity.getId(),
                entity.getLoteCodigo(),
                entrada,
                new PorcentajeDeMerma(entity.getMermaValor()),
                resultante,
                disponible,
                entity.getTostadorId(),
                entity.getFecha(),
                entity.getPerfilTuesteResultante()
        );
    }
}