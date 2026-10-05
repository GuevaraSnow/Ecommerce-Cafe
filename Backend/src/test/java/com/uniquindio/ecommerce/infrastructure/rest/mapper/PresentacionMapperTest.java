package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.dto.request.*;
import com.uniquindio.ecommerce.application.dto.response.*;
import com.uniquindio.ecommerce.application.usecase.ActualizarPresentacion;
import com.uniquindio.ecommerce.application.usecase.PublicarArticuloMerchandising;
import com.uniquindio.ecommerce.application.usecase.PublicarPresentacion;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.TransformacionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PresentacionMapperTest {

    private final PresentacionMapper mapper = new PresentacionMapper();
    private final PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
    private final LoteRepositorioMemoria lotes = new LoteRepositorioMemoria();
    private final TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();

    private List<ImagenRequest> galeria() {
        return List.of(new ImagenRequest("http://img/1.png", true), new ImagenRequest("http://img/2.png", false));
    }

    /** Publica con PublicarPresentacion usando el mapper para armar los parámetros desde el Request. */
    private Presentacion publicar(PublicarPresentacionRequest r, RolVendedor rol) {
        return new PublicarPresentacion(presentaciones, lotes, transformaciones).ejecutar(
                "pres-1", "vendedor-1", r.loteId(), r.transformacionId(), r.titulo(), r.tipo(),
                mapper.toPrecio(r.precio()), mapper.toCantidad(r.cantidad()), mapper.toGaleria(r.galeria()),
                r.perfilTueste(), rol, mapper.toFechaDeTueste(r.fechaTueste()));
    }

    private PublicarPresentacionRequest cafeVerdeDesdeLote() {
        lotes.guardar(Lote.registrar(new CodigoDeLote("LOT-2026-001"), new Cantidad(500, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL), new VariedadDeCafe("Caturra")));
        return new PublicarPresentacionRequest("Café Verde", TipoDePresentacion.CAFE_VERDE,
                new PrecioRequest(28000.0, "COP"), new CantidadRequest(200.0, "kg"), galeria(),
                "LOT-2026-001", null, null, null);
    }

    private PublicarPresentacionRequest cafeTostadoDesdeTransformacion() {
        transformaciones.guardar(Transformacion.registrar("transformacion-1", List.of("lote-1"),
                new Cantidad(50, "kg"), new PorcentajeDeMerma(0.18), "tostador-1", LocalDate.now(),
                PerfilTueste.MEDIO));
        return new PublicarPresentacionRequest("Café Tostado", TipoDePresentacion.CAFE_TOSTADO,
                new PrecioRequest(32000.0, "COP"), new CantidadRequest(20.0, "kg"), galeria(),
                null, "transformacion-1", PerfilTueste.MEDIO, LocalDate.now());
    }

    @Test
    void unaPresentacionTrazableProduceUnResponseConOrigenPerfilYFechaDeTueste() {
        Presentacion presentacion = publicar(cafeTostadoDesdeTransformacion(), RolVendedor.TOSTADOR);

        PresentacionResponse response = mapper.toResponse(presentacion);

        PresentacionTrazableResponse trazable = assertInstanceOf(PresentacionTrazableResponse.class, response);
        assertEquals("pres-1", trazable.id());
        assertEquals("vendedor-1", trazable.vendedorId());
        assertEquals("Café Tostado", trazable.titulo());
        assertEquals("CAFE_TOSTADO", trazable.tipo());
        assertEquals(new PrecioResponse(32000, "COP"), trazable.precio());
        assertEquals(new CantidadResponse(20, "kg"), trazable.cantidadDisponible());
        assertEquals(List.of(new ImagenResponse("http://img/1.png", true),
                new ImagenResponse("http://img/2.png", false)), trazable.galeria());
        assertEquals("ACTIVA", trazable.estado());
        assertFalse(trazable.eliminada());
        assertEquals(new OrigenResponse(null, "transformacion-1"), trazable.origen());
        assertEquals("MEDIO", trazable.perfilTueste());
        assertEquals(LocalDate.now(), trazable.fechaTueste());
        assertNull(trazable.notaCata());
    }

    @Test
    void elCafeVerdeDesdeUnLoteNoTraePerfilNiFechaDeTueste() {
        Presentacion presentacion = publicar(cafeVerdeDesdeLote(), RolVendedor.CAFICULTOR);

        PresentacionTrazableResponse trazable =
                assertInstanceOf(PresentacionTrazableResponse.class, mapper.toResponse(presentacion));

        assertEquals(new OrigenResponse("LOT-2026-001", null), trazable.origen());
        assertNull(trazable.perfilTueste());
        assertNull(trazable.fechaTueste());
    }

    @Test
    void laNotaDeCataRegistradaApareceEnElResponse() {
        PresentacionTrazable presentacion =
                (PresentacionTrazable) publicar(cafeVerdeDesdeLote(), RolVendedor.CAFICULTOR);
        presentacion.registrarNotaCata(mapper.toNotaDeCata(List.of("Chocolate", "Panela")));

        PresentacionTrazableResponse trazable =
                assertInstanceOf(PresentacionTrazableResponse.class, mapper.toResponse(presentacion));

        assertEquals(List.of("Chocolate", "Panela"), trazable.notaCata());
    }

    @Test
    void unArticuloDeMerchandisingProduceUnResponseSinOrigenNiDatosDeTueste() {
        PublicarArticuloMerchandisingRequest r = new PublicarArticuloMerchandisingRequest("Manilla",
                "Manilla artesanal", "semilla de café", new PrecioRequest(15000.0, "COP"),
                new CantidadRequest(20.0, "unidad"), galeria());
        Presentacion articulo = new PublicarArticuloMerchandising(presentaciones).ejecutar(
                "pres-2", "vendedor-1", r.titulo(), mapper.toPrecio(r.precio()), mapper.toCantidad(r.cantidad()),
                mapper.toGaleria(r.galeria()), r.material(), r.descripcion(), RolVendedor.VENDEDOR_DERIVADOS);

        PresentacionResponse response = mapper.toResponse(articulo);

        ArticuloDeMerchandisingResponse merch = assertInstanceOf(ArticuloDeMerchandisingResponse.class, response);
        assertEquals("MERCHANDISING", merch.tipo());
        assertEquals("Manilla artesanal", merch.descripcion());
        assertEquals("semilla de café", merch.material());
        assertEquals(new CantidadResponse(20, "unidad"), merch.cantidadDisponible());

        Set<String> campos = Arrays.stream(response.getClass().getRecordComponents())
                .map(RecordComponent::getName).collect(Collectors.toSet());
        assertFalse(campos.contains("origen"));
        assertFalse(campos.contains("perfilTueste"));
        assertFalse(campos.contains("fechaTueste"));
    }

    @Test
    void unaGaleriaSinImagenPrincipalPropagaReglaDominioException() {
        assertThrows(ReglaDominioException.class,
                () -> mapper.toGaleria(List.of(new ImagenRequest("http://img/1.png", false))));
    }

    @Test
    void unaGaleriaConDosImagenesPrincipalesOVaciaPropagaReglaDominioException() {
        assertThrows(ReglaDominioException.class, () -> mapper.toGaleria(
                List.of(new ImagenRequest("http://img/1.png", true), new ImagenRequest("http://img/2.png", true))));
        assertThrows(ReglaDominioException.class, () -> mapper.toGaleria(List.of()));
    }

    @Test
    void unPrecioOUnaCantidadInvalidosPropaganReglaDominioException() {
        assertThrows(ReglaDominioException.class, () -> mapper.toPrecio(new PrecioRequest(0.0, "COP")));
        assertThrows(ReglaDominioException.class, () -> mapper.toPrecio(new PrecioRequest(100.0, " ")));
        assertThrows(ReglaDominioException.class, () -> mapper.toCantidad(new CantidadRequest(-1.0, "kg")));
        assertThrows(ReglaDominioException.class, () -> mapper.toCantidad(new CantidadRequest(5.0, "toneladas")));
    }

    @Test
    void unaFechaDeTuesteFuturaOUnaNotaDeCataVaciaPropaganReglaDominioException() {
        assertThrows(ReglaDominioException.class, () -> mapper.toFechaDeTueste(LocalDate.now().plusDays(1)));
        assertThrows(ReglaDominioException.class, () -> mapper.toNotaDeCata(List.of()));
    }

    @Test
    void losValoresAusentesPasanComoNull() {
        assertNull(mapper.toPrecio(null));
        assertNull(mapper.toCantidad(null));
        assertNull(mapper.toGaleria(null));
        assertNull(mapper.toFechaDeTueste(null));
        assertNull(mapper.toNotaDeCata(null));
    }

    @Test
    void elRequestDeActualizarAplicaSoloLosCamposQueVienen() {
        publicar(cafeVerdeDesdeLote(), RolVendedor.CAFICULTOR);
        ActualizarPresentacion actualizar = new ActualizarPresentacion(presentaciones);
        ActualizarPresentacionRequest soloPausar = new ActualizarPresentacionRequest(null, null, false, null, null);

        Presentacion pausada = actualizar.ejecutar("pres-1", mapper.toPrecio(soloPausar.nuevoPrecio()),
                mapper.toCantidad(soloPausar.nuevaCantidadTotal()), soloPausar.activar(),
                mapper.toGaleria(soloPausar.nuevaGaleria()), mapper.toNotaDeCata(soloPausar.nuevaNotaCata()));

        PresentacionTrazableResponse response =
                assertInstanceOf(PresentacionTrazableResponse.class, mapper.toResponse(pausada));
        assertEquals("INACTIVA", response.estado());
        assertEquals(new PrecioResponse(28000, "COP"), response.precio());
        assertEquals(new CantidadResponse(200, "kg"), response.cantidadDisponible());
    }

    @Test
    void elRequestDeActualizarCambiaPrecioStockYNotaDeCata() {
        publicar(cafeVerdeDesdeLote(), RolVendedor.CAFICULTOR);
        ActualizarPresentacion actualizar = new ActualizarPresentacion(presentaciones);
        ActualizarPresentacionRequest r = new ActualizarPresentacionRequest(new PrecioRequest(30000.0, "COP"),
                new CantidadRequest(150.0, "kg"), null, null, List.of("Cítrico"));

        Presentacion actualizada = actualizar.ejecutar("pres-1", mapper.toPrecio(r.nuevoPrecio()),
                mapper.toCantidad(r.nuevaCantidadTotal()), r.activar(), mapper.toGaleria(r.nuevaGaleria()),
                mapper.toNotaDeCata(r.nuevaNotaCata()));

        PresentacionTrazableResponse response =
                assertInstanceOf(PresentacionTrazableResponse.class, mapper.toResponse(actualizada));
        assertEquals(new PrecioResponse(30000, "COP"), response.precio());
        assertEquals(new CantidadResponse(150, "kg"), response.cantidadDisponible());
        assertEquals(List.of("Cítrico"), response.notaCata());
        assertEquals("ACTIVA", response.estado());
    }
}
