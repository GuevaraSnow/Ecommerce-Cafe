package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import com.uniquindio.ecommerce.application.dto.request.UbicacionRequest;
import com.uniquindio.ecommerce.application.dto.response.FincaResponse;
import org.springframework.stereotype.Component;

@Component
public class FincaMapper {

    public Ubicacion toUbicacion(UbicacionRequest request) {
        if (request == null) return null;
        return new Ubicacion(request.departamento(), request.municipio());
    }

    public Altitud toAltitud(Number metros) {
        if (metros == null) return null;
        return new Altitud(metros.doubleValue());
    }

    public FincaResponse toResponse(Finca finca) {
        if (finca == null) return null;
        return new FincaResponse(
                finca.getId(),
                finca.getPropietarioId(),
                finca.getNombre(),
                finca.getUbicacion().departamento(),
                finca.getUbicacion().municipio(),
                (int) finca.getAltitud().metrosSobreNivelMar(),
                finca.isEliminada()
        );
    }
}