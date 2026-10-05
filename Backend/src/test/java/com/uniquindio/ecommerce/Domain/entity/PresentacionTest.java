package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del ciclo de vida común de Presentacion (Presentacion.publicar()
 * como despachador, y los métodos compartidos por ambas subclases). Se
 * ejercitan a través de ArticuloDeMerchandising por ser el subtipo más
 * simple; las reglas propias de cada subclase se prueban en sus propios
 * tests.
 */
class PresentacionTest {

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/manilla.png", true)));
    }

    private Presentacion publicarMerchandisingValido() {
        return Presentacion.publicar("pres-1", "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"), galeriaValida(), null,
                RolVendedor.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    @Test
    void noSePuedePublicarSinId() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar(" ", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void noSePuedePublicarSinVendedor() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", " ", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void noSePuedePublicarSinTitulo() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null, null,
                " ", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void noSePuedePublicarSinTipoDePresentacion() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null, null,
                "Manilla", null, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void noSePuedePublicarSinPrecio() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, null, new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void noSePuedePublicarSinCantidadDisponible() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), null,
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void noSePuedePublicarSinGaleria() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                null, null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void noSePuedePublicarSinRolDeVendedor() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, null, null, "material", "descripcion"));
    }

    @Test
    void unFormadorNoPuedePublicarNingunaPresentacion() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.FORMADOR, null, "material", "descripcion"));
    }

    @Test
    void unArticuloDeMerchandisingNoPuedeProvenirDeUnLote() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", "LOT-2026-045",
                null, "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material", "descripcion"));
    }

    @Test
    void unArticuloDeMerchandisingNoPuedeProvenirDeUnaTransformacion() {
        assertThrows(ReglaDominioException.class, () -> Presentacion.publicar("pres-1", "vendedor-1", null,
                "transformacion-1", "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"),
                new Cantidad(20, "unidad"), galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "material",
                "descripcion"));
    }

    @Test
    void descontarCantidadReduceElStockYMarcaAgotadaEnCero() {
        Presentacion presentacion = publicarMerchandisingValido();

        presentacion.descontarCantidad(new Cantidad(20, "unidad"));

        assertEquals(0, presentacion.getCantidadDisponible().valor());
        assertEquals(EstadoDePublicacion.AGOTADA, presentacion.getEstado());
    }

    @Test
    void reponerCantidadReactivaUnaPresentacionAgotada() {
        Presentacion presentacion = publicarMerchandisingValido();
        presentacion.descontarCantidad(new Cantidad(20, "unidad"));

        presentacion.reponerCantidad(new Cantidad(5, "unidad"));

        assertEquals(5, presentacion.getCantidadDisponible().valor());
        assertEquals(EstadoDePublicacion.ACTIVA, presentacion.getEstado());
    }

    @Test
    void cambiarPrecioNuloLanzaExcepcion() {
        Presentacion presentacion = publicarMerchandisingValido();

        assertThrows(ReglaDominioException.class, () -> presentacion.cambiarPrecio(null));
    }

    @Test
    void cambiarPrecioValidoActualizaElPrecio() {
        Presentacion presentacion = publicarMerchandisingValido();

        presentacion.cambiarPrecio(new Precio(20000, "COP"));

        assertEquals(new Precio(20000, "COP"), presentacion.getPrecio());
    }

    @Test
    void reemplazarGaleriaNulaLanzaExcepcion() {
        Presentacion presentacion = publicarMerchandisingValido();

        assertThrows(ReglaDominioException.class, () -> presentacion.reemplazarGaleria(null));
    }

    @Test
    void noSePuedePausarUnaPresentacionAgotadaYElEstadoNoCambia() {
        Presentacion presentacion = publicarMerchandisingValido();
        presentacion.descontarCantidad(new Cantidad(20, "unidad"));

        assertThrows(ReglaDominioException.class, presentacion::desactivar);

        assertEquals(EstadoDePublicacion.AGOTADA, presentacion.getEstado());
        assertFalse(presentacion.estaDisponibleParaVenta());
    }

    @Test
    void noSePuedeReactivarUnaPresentacionPausadaSinCantidadDisponible() {
        Presentacion presentacion = publicarMerchandisingValido();
        presentacion.desactivar();
        presentacion.descontarCantidad(new Cantidad(20, "unidad"));

        assertThrows(ReglaDominioException.class, presentacion::activar);

        assertEquals(EstadoDePublicacion.INACTIVA, presentacion.getEstado());
    }

    @Test
    void unaPresentacionPausadaQueSeQuedaSinStockSigueInactivaYSeReactivaAlReponer() {
        Presentacion presentacion = publicarMerchandisingValido();
        presentacion.desactivar();
        presentacion.descontarCantidad(new Cantidad(20, "unidad"));

        presentacion.reponerCantidad(new Cantidad(5, "unidad"));
        presentacion.activar();

        assertEquals(EstadoDePublicacion.ACTIVA, presentacion.getEstado());
    }

    @Test
    void desactivarYActivarCambianElEstado() {
        Presentacion presentacion = publicarMerchandisingValido();

        presentacion.desactivar();
        assertEquals(EstadoDePublicacion.INACTIVA, presentacion.getEstado());

        presentacion.activar();
        assertEquals(EstadoDePublicacion.ACTIVA, presentacion.getEstado());
    }

    @Test
    void noSePuedeActivarUnaPresentacionQueNoEstaInactiva() {
        Presentacion presentacion = publicarMerchandisingValido();

        assertThrows(ReglaDominioException.class, presentacion::activar);
    }

    @Test
    void darDeBajaImpideOperacionesPosteriores() {
        Presentacion presentacion = publicarMerchandisingValido();

        presentacion.darDeBaja();

        assertTrue(presentacion.isEliminada());
        assertThrows(ReglaDominioException.class, () -> presentacion.cambiarPrecio(new Precio(1, "COP")));
    }

    @Test
    void dosPresentacionesConElMismoIdSonIguales() {
        Presentacion p1 = publicarMerchandisingValido();
        Presentacion p2 = Presentacion.publicar("pres-1", "vendedor-2", null, null, "Otro título",
                TipoDePresentacion.MERCHANDISING, new Precio(1000, "COP"), new Cantidad(1, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null, "otro material", "otra descripcion");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void soloEstaDisponibleParaVentaSiEstaActivaYNoEliminada() {
        assertTrue(publicarMerchandisingValido().estaDisponibleParaVenta());

        Presentacion pausada = publicarMerchandisingValido();
        pausada.desactivar();
        assertFalse(pausada.estaDisponibleParaVenta());

        Presentacion agotada = publicarMerchandisingValido();
        agotada.descontarCantidad(new Cantidad(20, "unidad"));
        assertFalse(agotada.estaDisponibleParaVenta());

        Presentacion eliminada = publicarMerchandisingValido();
        eliminada.darDeBaja();
        assertFalse(eliminada.estaDisponibleParaVenta());
    }

    @Test
    void unaPresentacionReactivadaVuelveAEstarDisponibleParaVenta() {
        Presentacion presentacion = publicarMerchandisingValido();
        presentacion.desactivar();

        presentacion.activar();

        assertTrue(presentacion.estaDisponibleParaVenta());
    }
}
