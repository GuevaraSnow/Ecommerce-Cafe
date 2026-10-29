package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.RespuestaAResena;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.ResenaEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.RespuestaAResenaEmbeddable;
import org.springframework.stereotype.Component;

/** Convierte Resena (dominio) en ResenaEntity (tabla) y de vuelta, usando Resena.reconstruir(). */
@Component
public class ResenaPersistenceMapper {

    public ResenaEntity toEntity(Resena resena) {
        ResenaEntity entity = new ResenaEntity();
        entity.setId(resena.getId());
        entity.setCompradorId(resena.getCompradorId());
        entity.setPresentacionId(resena.getPresentacionId());
        entity.setVendedorId(resena.getVendedorId());
        entity.setCalificacion(resena.getCalificacion().valor());
        entity.setTexto(resena.getTexto().valor());
        entity.setFecha(resena.getFecha());
        entity.setRespuesta(resena.getRespuesta()
                .map(r -> new RespuestaAResenaEmbeddable(r.texto().valor(), r.fecha()))
                .orElse(null));
        return entity;
    }

    public Resena toDomain(ResenaEntity entity) {
        RespuestaAResenaEmbeddable respuesta = entity.getRespuesta();
        return Resena.reconstruir(
                entity.getId(),
                entity.getCompradorId(),
                entity.getPresentacionId(),
                entity.getVendedorId(),
                new Calificacion(entity.getCalificacion()),
                new TextoDeResena(entity.getTexto()),
                entity.getFecha(),
                respuesta == null || respuesta.getTexto() == null ? null
                        : new RespuestaAResena(new TextoDeResena(respuesta.getTexto()), respuesta.getFecha()));
    }
}
