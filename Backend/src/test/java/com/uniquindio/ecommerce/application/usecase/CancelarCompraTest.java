package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.CompraDigital;
import com.uniquindio.ecommerce.Domain.entity.CompraFisica;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
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

class CancelarCompraTest {

    private PresentacionRepositorioMemoria repositorioPresentacion;
    private CompraRepositorioMemoria repositorioCompra;
    private CancelarCompra cancelarCompra;
    private Presentacion primera;
    private Presentacion segunda;

    @BeforeEach
    void preparar() {
        repositorioPresentacion = new PresentacionRepositorioMemoria();
        repositorioCompra = new CompraRepositorioMemoria();
        cancelarCompra = new CancelarCompra(repositorioCompra, repositorioPresentacion);
        primera = presentacionDisponible("pres-1", 10);
        segunda = presentacionDisponible("pres-2", 5);
    }

    private Presentacion presentacionDisponible(String id, double kg) {
        Presentacion presentacion = Presentacion.publicar(
                id, "vendedor-1", "LOT-2026-001", null,
                "Cafe verde " + id, TipoDePresentacion.CAFE_VERDE,
                new Precio(50000, "COP"), new Cantidad(kg, "kg"),
                new Galeria(List.of(new ImagenDePresentacion("https://img.cafe/" + id + ".jpg", true))),
                null, RolVendedor.CAFICULTOR, null, null, null);
        repositorioPresentacion.guardar(presentacion);
        return presentacion;
    }

    private CompraFisica comprar(List<RealizarCompra.ItemSolicitado> items) {
        return new RealizarCompra(repositorioPresentacion, repositorioCompra).ejecutar(
                "comprador-1", items, "Montenegro", "Calle 10 # 5-20", "Carlos Perez");
    }

    private RealizarCompra.ItemSolicitado item(String presentacionId, double kg) {
        return new RealizarCompra.ItemSolicitado(presentacionId, new Cantidad(kg, "kg"));
    }

    @Test
    void cancelarUnaCompraDevuelveElStockYLaDejaCancelada() {
        CompraFisica compra = comprar(List.of(item("pres-1", 3)));
        assertEquals(new Cantidad(7, "kg"), primera.getCantidadDisponible());

        cancelarCompra.ejecutar(compra.getId());

        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
        assertEquals(EstadoDeCompra.CANCELADA, repositorioCompra.buscarPorId(compra.getId()).orElseThrow().getEstado());
    }

    @Test
    void cancelarUnaCompraConVariosItemsDevuelveElStockDeCadaPresentacion() {
        CompraFisica compra = comprar(List.of(item("pres-1", 3), item("pres-2", 2)));

        cancelarCompra.ejecutar(compra.getId());

        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
        assertEquals(new Cantidad(5, "kg"), segunda.getCantidadDisponible());
    }

    @Test
    void noSePuedeCancelarUnaCompraYaEnviadaYElStockNoCambia() {
        CompraFisica compra = comprar(List.of(item("pres-1", 3)));
        compra.confirmar();
        compra.marcarEnviada();

        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar(compra.getId()));

        assertEquals(new Cantidad(7, "kg"), primera.getCantidadDisponible());
        assertEquals(EstadoDeCompra.ENVIADA, compra.getEstado());
    }

    @Test
    void noSePuedeCancelarDosVecesNiDevolverElStockDosVeces() {
        CompraFisica compra = comprar(List.of(item("pres-1", 3)));
        cancelarCompra.ejecutar(compra.getId());

        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar(compra.getId()));

        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void cancelarUnaCompraConfirmadaTambienDevuelveElStock() {
        CompraFisica compra = comprar(List.of(item("pres-1", 3)));
        compra.confirmar();

        cancelarCompra.ejecutar(compra.getId());

        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void cancelarSigueFuncionandoSiUnaPresentacionFueDadaDeBaja() {
        CompraFisica compra = comprar(List.of(item("pres-1", 3)));
        primera.darDeBaja();

        cancelarCompra.ejecutar(compra.getId());

        assertEquals(EstadoDeCompra.CANCELADA, compra.getEstado());
        assertEquals(new Cantidad(7, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void siUnaPresentacionDelDetalleNoExisteNoSeCancelaNadaNiSeTocaElStock() {
        CompraFisica manual = CompraFisica.iniciar("comprador-1",
                new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        manual.agregarDetalle("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        manual.agregarDetalle("fantasma", new Precio(1000, "COP"), new Cantidad(1, "kg"));
        repositorioCompra.guardar(manual);

        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar(manual.getId()));

        assertEquals(EstadoDeCompra.PENDIENTE, manual.getEstado());
        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void noSePuedeCancelarUnaCompraInexistente() {
        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar("no-existe"));
    }

    @Test
    void noSePuedeCancelarAquiUnaCompraDigital() {
        CompraDigital digital = CompraDigital.iniciar("comprador-1", "curso-1", new Precio(50000, "COP"));
        repositorioCompra.guardar(digital);

        assertThrows(ReglaDominioException.class, () -> cancelarCompra.ejecutar(digital.getId()));
        assertEquals(EstadoDeCompra.PENDIENTE, digital.getEstado());
    }
}