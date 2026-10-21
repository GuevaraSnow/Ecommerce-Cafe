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

class ConfirmarCompraTest {

    private CompraRepositorioMemoria repositorioCompra;
    private ConfirmarCompra confirmarCompra;
    private String compraId;

    @BeforeEach
    void preparar() {
        PresentacionRepositorioMemoria repositorioPresentacion = new PresentacionRepositorioMemoria();
        repositorioCompra = new CompraRepositorioMemoria();
        Presentacion presentacion = Presentacion.publicar(
                "pres-1", "vendedor-1", "LOT-2026-001", null,
                "Cafe verde", TipoDePresentacion.CAFE_VERDE,
                new Precio(50000, "COP"), new Cantidad(10, "kg"),
                new Galeria(List.of(new ImagenDePresentacion("https://img.cafe/1.jpg", true))),
                null, RolVendedor.CAFICULTOR, null, null, null);
        repositorioPresentacion.guardar(presentacion);

        Compra compra = new RealizarCompra(repositorioPresentacion, repositorioCompra).ejecutar(
                "comprador-1",
                List.of(new RealizarCompra.ItemSolicitado("pres-1", new Cantidad(3, "kg"))));
        compraId = compra.getId();
        confirmarCompra = new ConfirmarCompra(repositorioCompra);
    }

    @Test
    void confirmarUnaCompraPendienteLaDejaConfirmadaEnElRepositorio() {
        confirmarCompra.ejecutar(compraId, "Montenegro", "Calle 10 # 5-20", "Carlos Perez");

        assertEquals(EstadoDeCompra.CONFIRMADA, repositorioCompra.buscarPorId(compraId).orElseThrow().getEstado());
    }

    @Test
    void confirmarGuardaLaDireccionDeEnvioEnLaCompra() {
        confirmarCompra.ejecutar(compraId, "Montenegro", "Calle 10 # 5-20", "Carlos Perez");

        assertEquals(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"),
                repositorioCompra.buscarPorId(compraId).orElseThrow().getDireccionEnvio().orElseThrow());
    }

    @Test
    void unaDireccionInvalidaNoConfirmaLaCompra() {
        assertThrows(ReglaDominioException.class,
                () -> confirmarCompra.ejecutar(compraId, "   ", "Calle 10 # 5-20", "Carlos Perez"));

        assertEquals(EstadoDeCompra.PENDIENTE, repositorioCompra.buscarPorId(compraId).orElseThrow().getEstado());
    }

    @Test
    void noSePuedeConfirmarDosVecesLaMismaCompra() {
        confirmarCompra.ejecutar(compraId, "Montenegro", "Calle 10 # 5-20", "Carlos Perez");

        assertThrows(ReglaDominioException.class, () -> confirmarCompra.ejecutar(compraId, "Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
    }

    @Test
    void noSePuedeConfirmarUnaCompraInexistente() {
        assertThrows(ReglaDominioException.class, () -> confirmarCompra.ejecutar("no-existe", "Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
    }
}