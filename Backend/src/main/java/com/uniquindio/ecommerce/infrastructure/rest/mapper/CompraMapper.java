package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.application.dto.request.CantidadRequest;
import com.uniquindio.ecommerce.application.dto.request.DireccionDeEnvioRequest;
import com.uniquindio.ecommerce.application.dto.response.CantidadResponse;
import com.uniquindio.ecommerce.application.dto.response.CompraResponse;
import com.uniquindio.ecommerce.application.dto.response.DetalleDeCompraResponse;
import com.uniquindio.ecommerce.application.dto.response.DireccionDeEnvioResponse;
import com.uniquindio.ecommerce.application.dto.response.PrecioResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Traduce entre los DTO de Compra y el dominio. No aplica reglas de negocio: los constructores
 * de los Value Objects validan y lanzan ReglaDominioException, que aquí nunca se captura.
 */
@Component
public class CompraMapper {

    public Cantidad toCantidad(CantidadRequest cantidad) {
        return cantidad == null ? null : new Cantidad(cantidad.valor(), cantidad.unidad());
    }

    public DireccionDeEnvio toDireccionDeEnvio(DireccionDeEnvioRequest direccion) {
        return direccion == null ? null
                : new DireccionDeEnvio(direccion.ciudad(), direccion.direccion(), direccion.destinatario());
    }

    public CompraResponse toResponse(Compra compra) {
        return new CompraResponse(
                compra.getId(),
                compra.getCompradorId(),
                compra.getEstado(),
                compra.getFechaCompra(),
                compra.getDetalles().stream().map(this::toResponse).toList(),
                compra.getDireccionEnvio()
                        .map(d -> new DireccionDeEnvioResponse(d.ciudad(), d.direccion(), d.destinatario()))
                        .orElse(null),
                compra.getFechaEntrega().orElse(null));
    }

    public List<CompraResponse> toResponses(List<Compra> compras) {
        return compras.stream().map(this::toResponse).toList();
    }

    private DetalleDeCompraResponse toResponse(DetalleDeCompra detalle) {
        return new DetalleDeCompraResponse(
                detalle.presentacionId(),
                new PrecioResponse(detalle.precioCongelado().monto(), detalle.precioCongelado().moneda()),
                new CantidadResponse(detalle.cantidad().valor(), detalle.cantidad().unidad()));
    }
}
