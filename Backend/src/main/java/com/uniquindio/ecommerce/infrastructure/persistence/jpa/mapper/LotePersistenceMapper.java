package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CantidadEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CosechaEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.LoteEntity;
import org.springframework.stereotype.Component;

@Component
public class LotePersistenceMapper {

    public LoteEntity toEntity(Lote lote) {
        LoteEntity entity = new LoteEntity();
        entity.setCodigoDeLote(lote.getCodigoDeLote().valor());
        entity.setFincaId(lote.getFincaID());
        entity.setCosecha(new CosechaEmbeddable(lote.getCosecha().anio(),lote.getCosecha().temporada()));
        entity.setProcesoDeBeneficio(lote.getProcesoDeBeneficio());
        entity.setVariedad(lote.getVariedad().nombre());
        entity.setCantidadDisponible(new CantidadEmbeddable(lote.getCantidadDisponible().valor(),
                lote.getCantidadDisponible().unidad()));
        entity.setEstado(lote.getEstado());
        entity.setEliminadoLogicamente(lote.isEliminadoLogicamente());
        return entity;
    }

    public Lote toDomain(LoteEntity entity) {
        return Lote.reconstruir(
                new CodigoDeLote(entity.getCodigoDeLote()),
                new Cantidad(entity.getCantidadDisponible().getValor(), entity.getCantidadDisponible().getUnidad() ),
                entity.getFincaId(),
                entity.getProcesoDeBeneficio(),
                entity.getEstado(),
                new Cosecha(entity.getCosecha().getAnio(), entity.getCosecha().getTemporada()),
                new VariedadDeCafe(entity.getVariedad()),
                entity.isEliminadoLogicamente()
                );
    }
}
