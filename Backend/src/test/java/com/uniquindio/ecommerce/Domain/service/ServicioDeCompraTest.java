package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ServicioDeCompraTest {

    private static final DireccionDeEnvio DIRECCION = new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez");

    private final PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
    private final ServicioDeCompra servicio = new ServicioDeCompra(presentaciones);

    private Presentacion primera;
    private Presentacion segunda;
    private Compra carrito;

    private Presentacion cafeVerde(String id, double kg) {
        Presentacion presentacion = Presentacion.publicar(id, "vendedor-1", "LOT-2026-001", null, "Cafe verde " + id,
                TipoDePresentacion.CAFE_VERDE, new Precio(50000, "COP"), new Cantidad(kg, "kg"),
                new Galeria(List.of(new ImagenDePresentacion("https://img.cafe/" + id + ".jpg", true))),
                null, RolVendedor.CAFICULTOR, null, null, null);
        presentaciones.guardar(presentacion);
        return presentacion;
    }

    @BeforeEach
    void preparar() {
        primera = cafeVerde("pres-1", 10);
        segunda = cafeVerde("pres-2", 5);
        carrito = Compra.abrirCarrito("compra-1", "comprador-1");
    }

    @Test
    void confirmarDejaLaCompraConfirmadaYDescuentaElStockDeCadaPresentacion() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        carrito.agregarPresentacion("pres-2", new Precio(50000, "COP"), new Cantidad(2, "kg"));

        servicio.confirmar(carrito, DIRECCION);

        assertEquals(EstadoDeCompra.CONFIRMADA, carrito.getEstado());
        assertEquals(new Cantidad(7, "kg"), primera.getCantidadDisponible());
        assertEquals(new Cantidad(3, "kg"), segunda.getCantidadDisponible());
    }

    @Test
    void siUnaPresentacionNoTieneStockSuficienteNoSeConfirmaNiSeDescuentaNada() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(20, "kg"));

        assertThrows(ReglaDominioException.class, () -> servicio.confirmar(carrito, DIRECCION));

        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void conStockInsuficienteEnElSegundoItemNoSeDescuentaNadaDelPrimero() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        carrito.agregarPresentacion("pres-2", new Precio(50000, "COP"), new Cantidad(8, "kg"));

        assertThrows(ReglaDominioException.class, () -> servicio.confirmar(carrito, DIRECCION));

        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
        assertEquals(new Cantidad(5, "kg"), segunda.getCantidadDisponible());
        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
    }

    @Test
    void siUnaPresentacionYaNoEstaALaVentaNoSeConfirmaNiSeDescuentaNada() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        primera.desactivar();

        assertThrows(ReglaDominioException.class, () -> servicio.confirmar(carrito, DIRECCION));

        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void sinDireccionDeEnvioNoSeConfirmaNiSeDescuentaNada() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));

        assertThrows(ReglaDominioException.class, () -> servicio.confirmar(carrito, null));

        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void unCarritoVacioNoSePuedeConfirmar() {
        assertThrows(ReglaDominioException.class, () -> servicio.confirmar(carrito, DIRECCION));

        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
    }

    @Test
    void siUnaPresentacionDelCarritoYaNoExisteSeLanzaNoEncontradoYNadaCambia() {
        Compra conFantasma = Compra.abrirCarrito("compra-2", "comprador-1");
        conFantasma.agregarPresentacion("no-existe", new Precio(50000, "COP"), new Cantidad(1, "kg"));

        assertThrows(NoSuchElementException.class, () -> servicio.confirmar(conFantasma, DIRECCION));

        assertEquals(EstadoDeCompra.PENDIENTE, conFantasma.getEstado());
    }

    @Test
    void cancelarUnaCompraConfirmadaReponeElStockDeCadaPresentacion() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        carrito.agregarPresentacion("pres-2", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        servicio.confirmar(carrito, DIRECCION);

        servicio.cancelar(carrito);

        assertEquals(EstadoDeCompra.CANCELADA, carrito.getEstado());
        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
        assertEquals(new Cantidad(5, "kg"), segunda.getCantidadDisponible());
    }

    @Test
    void cancelarUnCarritoPendienteNoTocaElStockPorqueNuncaSeDesconto() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));

        servicio.cancelar(carrito);

        assertEquals(EstadoDeCompra.CANCELADA, carrito.getEstado());
        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void unaPresentacionDadaDeBajaNoRecibeStockDeVueltaYLaCompraSeCancelaIgual() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        servicio.confirmar(carrito, DIRECCION);
        primera.darDeBaja();

        servicio.cancelar(carrito);

        assertEquals(EstadoDeCompra.CANCELADA, carrito.getEstado());
        assertEquals(new Cantidad(7, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void noSePuedeCancelarUnaCompraEnviadaYElStockNoCambia() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        servicio.confirmar(carrito, DIRECCION);
        carrito.enviar();

        assertThrows(ReglaDominioException.class, () -> servicio.cancelar(carrito));

        assertEquals(EstadoDeCompra.ENVIADA, carrito.getEstado());
        assertEquals(new Cantidad(7, "kg"), primera.getCantidadDisponible());
    }

    @Test
    void cancelarDosVecesNoReponeElStockDosVeces() {
        carrito.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        servicio.confirmar(carrito, DIRECCION);
        servicio.cancelar(carrito);

        assertThrows(ReglaDominioException.class, () -> servicio.cancelar(carrito));

        assertEquals(new Cantidad(10, "kg"), primera.getCantidadDisponible());
    }
}
