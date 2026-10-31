package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import com.uniquindio.ecommerce.application.dto.response.ResenaResponse;
import com.uniquindio.ecommerce.application.dto.response.RespuestaAResenaResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/** Traduce entre los DTO de Reseña y el dominio. */
@Component
public class ResenaMapper {

    public Calificacion toCalificacion(Integer calificacion) {
        return calificacion == null ? null : new Calificacion(calificacion);
    }

    public TextoDeResena toTexto(String texto) {
        return texto == null ? null : new TextoDeResena(texto);
    }

    public ResenaResponse toResponse(Resena resena) {
        return new ResenaResponse(
                resena.getId(),
                resena.getCompradorId(),
                resena.getPresentacionId(),
                resena.getVendedorId(),
                resena.getCalificacion().valor(),
                resena.getTexto().valor(),
                resena.getFecha(),
                resena.getRespuesta()
                        .map(r -> new RespuestaAResenaResponse(r.texto().valor(), r.fecha()))
                        .orElse(null));
    }

    public List<ResenaResponse> toResponses(List<Resena> resenas) {
        return resenas.stream().map(this::toResponse).toList();
    }
}
