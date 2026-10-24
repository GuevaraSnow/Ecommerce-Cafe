package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DetalleDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CantidadEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CompraEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.DetalleDeCompraEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.DireccionDeEnvioEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PrecioEmbeddable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/** Convierte Compra (dominio) en CompraEntity (tabla) y de vuelta, usando Compra.reconstruir(). */
@Component
public class CompraPersistenceMapper {

    public CompraEntity toEntity(Compra compra) {
        CompraEntity entity = new CompraEntity();
        entity.setId(compra.getId());
        entity.setCompradorId(compra.getCompradorId());
        entity.setFechaCompra(compra.getFechaCompra());
        entity.setEstado(compra.getEstado());
        entity.setDireccionEnvio(compra.getDireccionEnvio()
                .map(d -> new DireccionDeEnvioEmbeddable(d.ciudad(), d.direccion(), d.destinatario()))
                .orElse(null));
        entity.setFechaEntrega(compra.getFechaEntrega().orElse(null));
        entity.setDetalles(new ArrayList<>(compra.getDetalles().stream()
                .map(detalle -> new DetalleDeCompraEmbeddable(
                        detalle.presentacionId(),
                        new PrecioEmbeddable(detalle.precioCongelado().monto(), detalle.precioCongelado().moneda()),
                        new CantidadEmbeddable(detalle.cantidad().valor(), detalle.cantidad().unidad())))
                .toList()));
        return entity;
    }

    public Compra toDomain(CompraEntity entity) {
        DireccionDeEnvioEmbeddable direccion = entity.getDireccionEnvio();
        return Compra.reconstruir(
                entity.getId(),
                entity.getCompradorId(),
                entity.getFechaCompra(),
                direccion == null || direccion.getCiudad() == null ? null
                        : new DireccionDeEnvio(direccion.getCiudad(), direccion.getDireccion(), direccion.getDestinatario()),
                entity.getDetalles().stream()
                        .map(detalle -> new DetalleDeCompra(
                                entity.getId(),
                                detalle.getPresentacionId(),
                                new Precio(detalle.getPrecioCongelado().getMonto(), detalle.getPrecioCongelado().getMoneda()),
                                new Cantidad(detalle.getCantidad().getValor(), detalle.getCantidad().getUnidad())))
                        .toList(),
                entity.getEstado(),
                entity.getFechaEntrega());
    }
}
