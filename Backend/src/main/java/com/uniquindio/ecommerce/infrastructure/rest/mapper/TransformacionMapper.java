package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.FichaDeOrigen;
import com.uniquindio.ecommerce.application.dto.request.CantidadRequest;
import com.uniquindio.ecommerce.application.dto.response.CantidadResponse;
import com.uniquindio.ecommerce.application.dto.response.FichaDeOrigenResponse;
import com.uniquindio.ecommerce.application.dto.response.LoteResumenResponse;
import com.uniquindio.ecommerce.application.dto.response.TransformacionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransformacionMapper {

    public Cantidad toCantidad(CantidadRequest request) {
        if (request == null) return null;
        return new Cantidad(request.valor(), request.unidad());
    }

    public CantidadResponse toCantidadResponse(Cantidad cantidad) {
        if (cantidad == null) return null;
        return new CantidadResponse(cantidad.valor(), cantidad.unidad());
    }

    public TransformacionResponse toResponse(Transformacion transformacion) {
        if (transformacion == null) return null;
        return new TransformacionResponse(
                transformacion.getId(),
                transformacion.getLoteCodigo(),
                toCantidadResponse(transformacion.getCantidadEntrada()),
                transformacion.getMerma().valor(),
                toCantidadResponse(transformacion.getCantidadResultante()),
                toCantidadResponse(transformacion.getCantidadResultanteDisponible()),
                transformacion.getTostadorId(),
                transformacion.getFecha(),
                transformacion.getPerfilTuesteResultante()
        );
    }

    public FichaDeOrigenResponse toFichaResponse(FichaDeOrigen ficha) {
        if (ficha == null) return null;

        List<LoteResumenResponse> lotesResponse = ficha.lotesOrigen().stream()
                .map(lote -> new LoteResumenResponse(
                        lote.getCodigoDeLote().valor(),
                        lote.getFincaID(),
                        toCantidadResponse(lote.getCantidadDisponible()),
                        lote.getProcesoDeBeneficio().name(),
                        lote.getEstado().name()
                ))
                .toList();

        return new FichaDeOrigenResponse(
                ficha.fincaNombre(),
                ficha.cosechaInfo(),
                lotesResponse,
                ficha.transformacionId(),
                ficha.tostadorId(),
                ficha.fechaTransformacion(),
                ficha.perfilTuesteResultante()
        );
    }
}