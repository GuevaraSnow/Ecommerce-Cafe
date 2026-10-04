package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.Cosecha;
import com.uniquindio.ecommerce.Domain.valueobject.VariedadDeCafe;
import com.uniquindio.ecommerce.application.dto.request.RegistrarLoteRequest;
import com.uniquindio.ecommerce.application.dto.response.LoteResponse;
import org.springframework.stereotype.Component;

/**
 * Traduce entre los DTO de Lote y el dominio. No aplica reglas de negocio: los
 * constructores de los value objects son quienes validan y lanzan
 * ReglaDominioException, que aquí nunca se captura.
 */
@Component
public class LoteMapper {

    public CodigoDeLote toCodigoDeLote(String codigo) {
        return new CodigoDeLote(codigo);
    }

    public Cantidad toCantidad(RegistrarLoteRequest.CantidadRequest cantidad) {
        return new Cantidad(cantidad.valor(), cantidad.unidad());
    }

    public Cosecha toCosecha(RegistrarLoteRequest.CosechaRequest cosecha) {
        return new Cosecha(cosecha.anio(), cosecha.temporada());
    }

    public VariedadDeCafe toVariedad(String variedad) {
        return new VariedadDeCafe(variedad);
    }

    public LoteResponse toResponse(Lote lote) {
        return new LoteResponse(
                lote.getCodigoDeLote().valor(),
                lote.getFincaID(),
                new LoteResponse.CantidadResponse(
                        lote.getCantidadDisponible().valor(),
                        lote.getCantidadDisponible().unidad()),
                lote.getEstado().name(),
                lote.getProcesoDeBeneficio().name(),
                lote.getVariedad().nombre(),
                new LoteResponse.CosechaResponse(
                        lote.getCosecha().anio(),
                        lote.getCosecha().temporada().name()),
                lote.isEliminadoLogicamente());
    }
}
