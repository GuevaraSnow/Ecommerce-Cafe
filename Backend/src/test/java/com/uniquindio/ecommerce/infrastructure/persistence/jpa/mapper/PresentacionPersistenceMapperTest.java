package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.ArticuloMerchandisingEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PresentacionEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.PresentacionTrazableEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** El mapper no pierde ningún campo al ir del dominio a la tabla y volver. */
class PresentacionPersistenceMapperTest {

    private final PresentacionPersistenceMapper mapper = new PresentacionPersistenceMapper();

    private Galeria galeria() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/1.png", true),
                new ImagenDePresentacion("http://img/2.png", false), new ImagenDePresentacion("http://img/3.png", false)));
    }

    private PresentacionTrazable trazableCompleta() {
        PresentacionTrazable presentacion = (PresentacionTrazable) Presentacion.publicar("pres-1", "vendedor-1", null,
                "transformacion-1", "Café Tostado", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(20, "kg"), galeria(), PerfilTueste.MEDIO, Rol.TOSTADOR,
                new FechaDeTueste(LocalDate.now().minusDays(4)), null, null);
        presentacion.registrarNotaCata(new NotaDeCata(List.of("Chocolate", "Panela", "Cítrico")));
        presentacion.programarDescuento(new Descuento(15, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)));
        presentacion.desactivar();
        return presentacion;
    }

    private ArticuloDeMerchandising articulo() {
        Presentacion presentacion = Presentacion.publicar("pres-2", "vendedor-2", null, null, "Manilla",
                TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"), galeria(),
                null, Rol.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
        return (ArticuloDeMerchandising) presentacion;
    }

    @Test
    void unaTrazableSeConvierteEnLaFilaTrazableConTodosSusCampos() {
        PresentacionEntity entity = mapper.toEntity(trazableCompleta());

        PresentacionTrazableEntity fila = assertInstanceOf(PresentacionTrazableEntity.class, entity);
        assertEquals("pres-1", fila.getId());
        assertEquals("vendedor-1", fila.getVendedorId());
        assertEquals("Café Tostado", fila.getTitulo());
        assertEquals(TipoDePresentacion.CAFE_TOSTADO, fila.getTipo());
        assertEquals(32000, fila.getPrecio().getMonto());
        assertEquals("COP", fila.getPrecio().getMoneda());
        assertEquals(20, fila.getCantidadDisponible().getValor());
        assertEquals("kg", fila.getCantidadDisponible().getUnidad());
        assertEquals(EstadoDePublicacion.INACTIVA, fila.getEstado());
        assertFalse(fila.isEliminada());
        assertEquals(15, fila.getDescuento().getPorcentaje());
        assertNull(fila.getLoteId());
        assertEquals("transformacion-1", fila.getTransformacionId());
        assertEquals(PerfilTueste.MEDIO, fila.getPerfilTueste());
        assertEquals(LocalDate.now().minusDays(4), fila.getFechaTueste());
        assertEquals(List.of("Chocolate", "Panela", "Cítrico"), fila.getNotaDeCata());
        assertEquals(List.of("http://img/1.png", "http://img/2.png", "http://img/3.png"),
                fila.getGaleria().stream().map(imagen -> imagen.getUrl()).toList());
    }

    @Test
    void unaTrazableVuelveDeLaTablaIgualQueSalio() {
        PresentacionTrazable original = trazableCompleta();

        PresentacionTrazable devuelta = assertInstanceOf(PresentacionTrazable.class,
                mapper.toDomain(mapper.toEntity(original)));

        assertEquals(original.getId(), devuelta.getId());
        assertEquals(original.getVendedorId(), devuelta.getVendedorId());
        assertEquals(original.getTitulo(), devuelta.getTitulo());
        assertEquals(original.getTipoPresentacion(), devuelta.getTipoPresentacion());
        assertEquals(original.getPrecio(), devuelta.getPrecio());
        assertEquals(original.getCantidadDisponible(), devuelta.getCantidadDisponible());
        assertEquals(original.getGaleria(), devuelta.getGaleria());
        assertEquals(original.getEstado(), devuelta.getEstado());
        assertEquals(original.isEliminada(), devuelta.isEliminada());
        assertEquals(original.getDescuento(), devuelta.getDescuento());
        assertEquals(original.getOrigen(), devuelta.getOrigen());
        assertEquals(original.getPerfilDeTueste(), devuelta.getPerfilDeTueste());
        assertEquals(original.getFechaTueste(), devuelta.getFechaTueste());
        assertEquals(original.getNotaDeCata(), devuelta.getNotaDeCata());
    }

    @Test
    void unaTrazableSinNotaNiDescuentoVuelveConEsosCamposAusentes() {
        Presentacion original = Presentacion.publicar("pres-3", "vendedor-1", "LOT-2026-001", null, "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeria(), null,
                Rol.CAFICULTOR, null, null, null);

        PresentacionTrazable devuelta = assertInstanceOf(PresentacionTrazable.class,
                mapper.toDomain(mapper.toEntity(original)));

        assertNull(devuelta.getNotaDeCata());
        assertNull(devuelta.getDescuento());
        assertNull(devuelta.getFechaTueste());
        assertNull(devuelta.getPerfilDeTueste());
        assertEquals(new OrigenDePresentacion("LOT-2026-001", null), devuelta.getOrigen());
    }

    @Test
    void unArticuloSeConvierteEnLaFilaDeMerchandisingYVuelveIgual() {
        ArticuloDeMerchandising original = articulo();
        original.programarDescuento(new Descuento(10, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31)));

        PresentacionEntity entity = mapper.toEntity(original);
        ArticuloMerchandisingEntity fila = assertInstanceOf(ArticuloMerchandisingEntity.class, entity);
        ArticuloDeMerchandising devuelto = assertInstanceOf(ArticuloDeMerchandising.class, mapper.toDomain(entity));

        assertEquals("semilla de café", fila.getMaterial());
        assertEquals("Manilla artesanal", fila.getDescripcion());
        assertEquals(original.getId(), devuelto.getId());
        assertEquals(original.getPrecio(), devuelto.getPrecio());
        assertEquals(original.getCantidadDisponible(), devuelto.getCantidadDisponible());
        assertEquals(original.getGaleria(), devuelto.getGaleria());
        assertEquals(original.getDescuento(), devuelto.getDescuento());
        assertEquals(original.getMaterial(), devuelto.getMaterial());
        assertEquals(original.getDescripcion(), devuelto.getDescripcion());
    }

    @Test
    void laBajaLogicaYElEstadoAgotadoSeConservan() {
        ArticuloDeMerchandising original = articulo();
        original.descontarCantidad(new Cantidad(20, "unidad"));
        original.darDeBaja();

        Presentacion devuelta = mapper.toDomain(mapper.toEntity(original));

        assertTrue(devuelta.isEliminada());
        assertEquals(EstadoDePublicacion.AGOTADA, devuelta.getEstado());
        assertEquals(new Cantidad(0, "unidad"), devuelta.getCantidadDisponible());
    }

    @Test
    void unaTrazableVencidaSeGuardaComoVencida() {
        PresentacionTrazable vencida = PresentacionTrazable.reconstruir("pres-4", "vendedor-1", "Café Tostado",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(5, "kg"), galeria(),
                EstadoDePublicacion.ACTIVA, false, null, new OrigenDePresentacion(null, "transformacion-1"),
                PerfilTueste.MEDIO, new FechaDeTueste(LocalDate.now().minusDays(50)), null);

        assertEquals(EstadoDePublicacion.VENCIDA, mapper.toEntity(vencida).getEstado());
    }
}
