package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.Descuento;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeTueste;
import com.uniquindio.ecommerce.Domain.valueobject.Galeria;
import com.uniquindio.ecommerce.Domain.valueobject.ImagenDePresentacion;
import com.uniquindio.ecommerce.Domain.valueobject.NotaDeCata;
import com.uniquindio.ecommerce.Domain.valueobject.OrigenDePresentacion;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.ArticuloMerchandisingEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.CantidadEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.DescuentoEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.ImagenEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PrecioEmbeddable;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PresentacionEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PresentacionTrazableEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Convierte Presentacion (dominio) en PresentacionEntity (tabla) y de vuelta, usando los
 * {@code reconstruir} de cada subclase. Al guardar se persiste el estado que informa el dominio
 * (para una Trazable, el calculado: una vencida se guarda como VENCIDA).
 */
@Component
public class PresentacionPersistenceMapper {

    public PresentacionEntity toEntity(Presentacion presentacion) {
        if (presentacion instanceof PresentacionTrazable trazable) {
            return toTrazableEntity(trazable);
        }
        if (presentacion instanceof ArticuloDeMerchandising articulo) {
            return toMerchandisingEntity(articulo);
        }
        throw new IllegalArgumentException("Tipo de Presentación no soportado: " + presentacion.getClass().getName());
    }

    public Presentacion toDomain(PresentacionEntity entity) {
        if (entity instanceof PresentacionTrazableEntity trazable) {
            return toTrazable(trazable);
        }
        if (entity instanceof ArticuloMerchandisingEntity articulo) {
            return toMerchandising(articulo);
        }
        throw new IllegalArgumentException("Tipo de fila no soportado: " + entity.getClass().getName());
    }

    private PresentacionTrazableEntity toTrazableEntity(PresentacionTrazable p) {
        PresentacionTrazableEntity entity = new PresentacionTrazableEntity();
        llenarComunes(entity, p);
        entity.setLoteId(p.getOrigen().loteId());
        entity.setTransformacionId(p.getOrigen().transformacionId());
        entity.setPerfilTueste(p.getPerfilDeTueste());
        entity.setFechaTueste(p.getFechaTueste() == null ? null : p.getFechaTueste().valor());
        entity.setNotaDeCata(p.getNotaDeCata() == null ? new ArrayList<>() : new ArrayList<>(p.getNotaDeCata().descriptores()));
        return entity;
    }

    private ArticuloMerchandisingEntity toMerchandisingEntity(ArticuloDeMerchandising a) {
        ArticuloMerchandisingEntity entity = new ArticuloMerchandisingEntity();
        llenarComunes(entity, a);
        entity.setMaterial(a.getMaterial());
        entity.setDescripcion(a.getDescripcion());
        return entity;
    }

    private void llenarComunes(PresentacionEntity entity, Presentacion p) {
        entity.setId(p.getId());
        entity.setVendedorId(p.getVendedorId());
        entity.setTitulo(p.getTitulo());
        entity.setTipo(p.getTipoPresentacion());
        entity.setPrecio(new PrecioEmbeddable(p.getPrecio().monto(), p.getPrecio().moneda()));
        entity.setCantidadDisponible(
                new CantidadEmbeddable(p.getCantidadDisponible().valor(), p.getCantidadDisponible().unidad()));
        entity.setDescuento(p.getDescuento() == null ? null
                : new DescuentoEmbeddable(p.getDescuento().porcentaje(), p.getDescuento().fechaInicio(),
                p.getDescuento().fechaFin()));
        entity.setEstado(p.getEstado());
        entity.setEliminada(p.isEliminada());
        entity.setGaleria(new ArrayList<>(p.getGaleria().imagenes().stream()
                .map(imagen -> new ImagenEmbeddable(imagen.url(), imagen.principal()))
                .toList()));
    }

    private PresentacionTrazable toTrazable(PresentacionTrazableEntity e) {
        return PresentacionTrazable.reconstruir(e.getId(), e.getVendedorId(), e.getTitulo(), e.getTipo(),
                toPrecio(e), toCantidad(e), toGaleria(e), e.getEstado(), e.isEliminada(), toDescuento(e),
                new OrigenDePresentacion(e.getLoteId(), e.getTransformacionId()), e.getPerfilTueste(),
                e.getFechaTueste() == null ? null : new FechaDeTueste(e.getFechaTueste()),
                e.getNotaDeCata().isEmpty() ? null : new NotaDeCata(e.getNotaDeCata()));
    }

    private ArticuloDeMerchandising toMerchandising(ArticuloMerchandisingEntity e) {
        return ArticuloDeMerchandising.reconstruir(e.getId(), e.getVendedorId(), e.getTitulo(), e.getTipo(),
                toPrecio(e), toCantidad(e), toGaleria(e), e.getEstado(), e.isEliminada(), toDescuento(e),
                e.getMaterial(), e.getDescripcion());
    }

    private Precio toPrecio(PresentacionEntity e) {
        return new Precio(e.getPrecio().getMonto(), e.getPrecio().getMoneda());
    }

    private Cantidad toCantidad(PresentacionEntity e) {
        return new Cantidad(e.getCantidadDisponible().getValor(), e.getCantidadDisponible().getUnidad());
    }

    private Galeria toGaleria(PresentacionEntity e) {
        List<ImagenDePresentacion> imagenes = e.getGaleria().stream()
                .map(imagen -> new ImagenDePresentacion(imagen.getUrl(), imagen.isPrincipal()))
                .toList();
        return new Galeria(imagenes);
    }

    private Descuento toDescuento(PresentacionEntity e) {
        DescuentoEmbeddable d = e.getDescuento();
        return d == null || d.getPorcentaje() == null ? null
                : new Descuento(d.getPorcentaje(), d.getFechaInicio(), d.getFechaFin());
    }
}
