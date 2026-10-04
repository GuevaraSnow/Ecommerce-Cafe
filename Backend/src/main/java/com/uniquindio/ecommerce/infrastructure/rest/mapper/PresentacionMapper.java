package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeTueste;
import com.uniquindio.ecommerce.Domain.valueobject.Galeria;
import com.uniquindio.ecommerce.Domain.valueobject.ImagenDePresentacion;
import com.uniquindio.ecommerce.Domain.valueobject.NotaDeCata;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.application.dto.request.CantidadRequest;
import com.uniquindio.ecommerce.application.dto.request.ImagenRequest;
import com.uniquindio.ecommerce.application.dto.request.PrecioRequest;
import com.uniquindio.ecommerce.application.dto.response.ArticuloDeMerchandisingResponse;
import com.uniquindio.ecommerce.application.dto.response.CantidadResponse;
import com.uniquindio.ecommerce.application.dto.response.ImagenResponse;
import com.uniquindio.ecommerce.application.dto.response.OrigenResponse;
import com.uniquindio.ecommerce.application.dto.response.PrecioResponse;
import com.uniquindio.ecommerce.application.dto.response.PresentacionResponse;
import com.uniquindio.ecommerce.application.dto.response.PresentacionTrazableResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Traduce entre los DTO de Presentación y el dominio. No aplica reglas de negocio:
 * los constructores de los value objects validan y lanzan ReglaDominioException, que
 * aquí nunca se captura. Un valor ausente (null) pasa como null; si el dominio lo
 * exige, es él quien lo rechaza.
 */
@Component
public class PresentacionMapper {

    public Precio toPrecio(PrecioRequest precio) {
        return precio == null ? null : new Precio(precio.monto(), precio.moneda());
    }

    public Cantidad toCantidad(CantidadRequest cantidad) {
        return cantidad == null ? null : new Cantidad(cantidad.valor(), cantidad.unidad());
    }

    public Galeria toGaleria(List<ImagenRequest> imagenes) {
        if (imagenes == null) {
            return null;
        }
        return new Galeria(imagenes.stream()
                .map(imagen -> new ImagenDePresentacion(imagen.url(), imagen.principal()))
                .toList());
    }

    public FechaDeTueste toFechaDeTueste(LocalDate fecha) {
        return fecha == null ? null : new FechaDeTueste(fecha);
    }

    public NotaDeCata toNotaDeCata(List<String> descriptores) {
        return descriptores == null ? null : new NotaDeCata(descriptores);
    }

    public PresentacionResponse toResponse(Presentacion presentacion) {
        if (presentacion instanceof PresentacionTrazable trazable) {
            return toTrazableResponse(trazable);
        }
        if (presentacion instanceof ArticuloDeMerchandising articulo) {
            return toMerchandisingResponse(articulo);
        }
        throw new IllegalArgumentException("Tipo de Presentación no soportado: " + presentacion.getClass().getName());
    }

    private PresentacionTrazableResponse toTrazableResponse(PresentacionTrazable p) {
        return new PresentacionTrazableResponse(
                p.getId(),
                p.getVendedorId(),
                p.getTitulo(),
                p.getTipoPresentacion().name(),
                toPrecioResponse(p.getPrecio()),
                toCantidadResponse(p.getCantidadDisponible()),
                toImagenesResponse(p.getGaleria()),
                p.getEstado().name(),
                p.isEliminada(),
                new OrigenResponse(p.getOrigen().loteId(), p.getOrigen().transformacionId()),
                p.getPerfilDeTueste() == null ? null : p.getPerfilDeTueste().name(),
                p.getFechaTueste() == null ? null : p.getFechaTueste().valor(),
                p.getNotaDeCata() == null ? null : p.getNotaDeCata().descriptores());
    }

    private ArticuloDeMerchandisingResponse toMerchandisingResponse(ArticuloDeMerchandising a) {
        return new ArticuloDeMerchandisingResponse(
                a.getId(),
                a.getVendedorId(),
                a.getTitulo(),
                a.getTipoPresentacion().name(),
                toPrecioResponse(a.getPrecio()),
                toCantidadResponse(a.getCantidadDisponible()),
                toImagenesResponse(a.getGaleria()),
                a.getEstado().name(),
                a.isEliminada(),
                a.getDescripcion(),
                a.getMaterial());
    }

    private PrecioResponse toPrecioResponse(Precio precio) {
        return new PrecioResponse(precio.monto(), precio.moneda());
    }

    private CantidadResponse toCantidadResponse(Cantidad cantidad) {
        return new CantidadResponse(cantidad.valor(), cantidad.unidad());
    }

    private List<ImagenResponse> toImagenesResponse(Galeria galeria) {
        return galeria.imagenes().stream()
                .map(imagen -> new ImagenResponse(imagen.url(), imagen.principal()))
                .toList();
    }
}
