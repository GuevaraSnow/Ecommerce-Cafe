package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
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

class CambiarEstadoCompraTest {

    private CompraRepositorioMemoria repositorioCompra;
    private CambiarEstadoCompra cambiarEstadoCompra;
    private String compraId;

    @BeforeEach
    void preparar() {
        repositorioCompra = new CompraRepositorioMemoria();
        Compra compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(3, "kg"));
        compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        repositorioCompra.guardar(compra);
        compraId = compra.getId();
        cambiarEstadoCompra = new CambiarEstadoCompra(repositorioCompra);
    }

    @Test
    void unaCompraConfirmadaPuedePasarAEnviada() {
        cambiarEstadoCompra.ejecutar(compraId, EstadoDeCompra.ENVIADA);

        assertEquals(EstadoDeCompra.ENVIADA, repositorioCompra.buscarPorId(compraId).orElseThrow().getEstado());
    }

    @Test
    void unaCompraEnviadaPuedePasarAEntregadaYRegistraLaFecha() {
        cambiarEstadoCompra.ejecutar(compraId, EstadoDeCompra.ENVIADA);

        Compra compra = cambiarEstadoCompra.ejecutar(compraId, EstadoDeCompra.ENTREGADA);

        assertEquals(EstadoDeCompra.ENTREGADA, compra.getEstado());
        assertTrue(compra.getFechaEntrega().isPresent());
    }

    @Test
    void noSePuedeSaltarDeConfirmadaAEntregada() {
        assertThrows(ReglaDominioException.class,
                () -> cambiarEstadoCompra.ejecutar(compraId, EstadoDeCompra.ENTREGADA));

        assertEquals(EstadoDeCompra.CONFIRMADA, repositorioCompra.buscarPorId(compraId).orElseThrow().getEstado());
    }

    @Test
    void cancelarConfirmarOReembolsarTienenSuPropioCasoDeUso() {
        for (EstadoDeCompra estado : List.of(EstadoDeCompra.PENDIENTE, EstadoDeCompra.CONFIRMADA,
                EstadoDeCompra.CANCELADA, EstadoDeCompra.REEMBOLSADA)) {
            assertThrows(ReglaDominioException.class, () -> cambiarEstadoCompra.ejecutar(compraId, estado));
        }

        assertEquals(EstadoDeCompra.CONFIRMADA, repositorioCompra.buscarPorId(compraId).orElseThrow().getEstado());
    }

    @Test
    void noSePuedeCambiarElEstadoDeUnaCompraInexistente() {
        assertThrows(ReglaDominioException.class,
                () -> cambiarEstadoCompra.ejecutar("no-existe", EstadoDeCompra.ENVIADA));
    }

    @Test
    void noSePuedeCambiarElEstadoSinIndicarElNuevoEstado() {
        assertThrows(ReglaDominioException.class, () -> cambiarEstadoCompra.ejecutar(compraId, null));
    }
}