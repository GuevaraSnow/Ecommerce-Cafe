package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Galeria;
import com.uniquindio.ecommerce.Domain.valueobject.ImagenDePresentacion;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.RolVendedor;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import com.uniquindio.ecommerce.infrastructure.persistence.CompraRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RealizarCompraTest {

    private PresentacionRepositorioMemoria repositorioPresentacion;
    private CompraRepositorioMemoria repositorioCompra;
    private RealizarCompra realizarCompra;

    @BeforeEach
    void preparar() {
        repositorioPresentacion = new PresentacionRepositorioMemoria();
        repositorioCompra = new CompraRepositorioMemoria();
        realizarCompra = new RealizarCompra(repositorioPresentacion, repositorioCompra);
    }

    private Presentacion presentacionDisponible(String id, double kg, double precio) {
        Presentacion presentacion = Presentacion.publicar(
                id, "vendedor-1", "LOT-2026-001", null,
                "Cafe verde " + id, TipoDePresentacion.CAFE_VERDE,
                new Precio(precio, "COP"), new Cantidad(kg, "kg"),
                new Galeria(List.of(new ImagenDePresentacion("https://img.cafe/" + id + ".jpg", true))),
                null, RolVendedor.CAFICULTOR, null);
        repositorioPresentacion.guardar(presentacion);
        return presentacion;
    }

    private RealizarCompra.ItemSolicitado item(String presentacionId, double kg) {
        return new RealizarCompra.ItemSolicitado(presentacionId, new Cantidad(kg, "kg"));
    }

    private Compra comprar(List<RealizarCompra.ItemSolicitado> items) {
        return realizarCompra.ejecutar("comprador-1", items, "Montenegro", "Calle 10 # 5-20", "Carlos Perez");
    }

    @Test
    void comprarDescuentaElStockYDejaLaCompraGuardadaYPendiente() {
        Presentacion presentacion = presentacionDisponible("pres-1", 10, 50000);

        Compra compra = comprar(List.of(item("pres-1", 3)));

        assertEquals(new Cantidad(7, "kg"), presentacion.getCantidadDisponible());
        assertEquals(EstadoDeCompra.PENDIENTE, compra.getEstado());
        assertTrue(repositorioCompra.buscarPorId(compra.getId()).isPresent());
    }

    @Test
    void elDetalleCongelaElPrecioVigenteAlMomentoDeComprar() {
        Presentacion presentacion = presentacionDisponible("pres-1", 10, 50000);

        Compra compra = comprar(List.of(item("pres-1", 2)));
        presentacion.cambiarPrecio(new Precio(80000, "COP"));

        assertEquals(new Precio(50000, "COP"), compra.getDetalles().get(0).precioCongelado());
    }

    @Test
    void unaCompraConVariosItemsDescuentaCadaPresentacion() {
        Presentacion primera = presentacionDisponible("pres-1", 10, 50000);
        Presentacion segunda = presentacionDisponible("pres-2", 5, 30000);

        Compra compra = comprar(List.of(item("pres-1", 3), item("pres-2", 2)));

        assertEquals(2, compra.getDetalles().size());
        assertEquals(new Cantidad(7, "kg"), primera.getCantidadDisponible());
        assertEquals(new Cantidad(3, "kg"), segunda.getCantidadDisponible());
    }

    @Test
    void noSePuedeComprarMasDeLoDisponibleYElStockNoCambia() {
        Presentacion presentacion = presentacionDisponible("pres-1", 10, 50000);

        assertThrows(ReglaDominioException.class, () -> comprar(List.of(item("pres-1", 20))));

        assertEquals(new Cantidad(10, "kg"), presentacion.getCantidadDisponible());
    }

    @Test
    void siUnItemFallaNoSeDescuentaElStockDeLosDemas() {
        Presentacion alcanza = presentacionDisponible("pres-1", 10, 50000);
        presentacionDisponible("pres-2", 5, 30000);

        assertThrows(ReglaDominioException.class,
                () -> comprar(List.of(item("pres-1", 3), item("pres-2", 100))));

        assertEquals(new Cantidad(10, "kg"), alcanza.getCantidadDisponible());
    }

    @Test
    void noSePuedeComprarUnaPresentacionInexistente() {
        assertThrows(ReglaDominioException.class, () -> comprar(List.of(item("no-existe", 1))));
    }

    @Test
    void noSePuedeComprarUnaPresentacionDadaDeBajaYElStockDeLasOtrasNoCambia() {
        Presentacion activa = presentacionDisponible("pres-1", 10, 50000);
        Presentacion dadaDeBaja = presentacionDisponible("pres-2", 5, 30000);
        dadaDeBaja.darDeBaja();

        assertThrows(ReglaDominioException.class,
                () -> comprar(List.of(item("pres-1", 3), item("pres-2", 1))));

        assertEquals(new Cantidad(10, "kg"), activa.getCantidadDisponible());
    }

    @Test
    void noSePuedeRealizarUnaCompraSinItems() {
        assertThrows(ReglaDominioException.class, () -> comprar(List.of()));
        assertThrows(ReglaDominioException.class, () -> comprar(null));
    }

    @Test
    void unaDireccionInvalidaImpideLaCompraYNoTocaElStock() {
        Presentacion presentacion = presentacionDisponible("pres-1", 10, 50000);

        assertThrows(ReglaDominioException.class, () -> realizarCompra.ejecutar(
                "comprador-1", List.of(item("pres-1", 3)), "   ", "Calle 10 # 5-20", "Carlos Perez"));

        assertEquals(new Cantidad(10, "kg"), presentacion.getCantidadDisponible());
    }
}