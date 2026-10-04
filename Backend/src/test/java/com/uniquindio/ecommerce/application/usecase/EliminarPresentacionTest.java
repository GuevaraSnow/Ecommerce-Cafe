package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.CompraDigital;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.CompraRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EliminarPresentacionTest {

    private void publicarMerchandising(PresentacionRepositorioMemoria repositorio, String id) {
        new PublicarArticuloMerchandising(repositorio).ejecutar(id, "vendedor-1", "Manilla", new Precio(15000, "COP"),
                new Cantidad(20, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/manilla.png", true))),
                "semilla de café", "Manilla artesanal", RolVendedor.VENDEDOR_DERIVADOS);
    }

    private void comprar(PresentacionRepositorioMemoria presentaciones, CompraRepositorioMemoria compras,
                         String presentacionId) {
        new RealizarCompra(presentaciones, compras).ejecutar("comprador-1",
                List.of(new RealizarCompra.ItemSolicitado(presentacionId, new Cantidad(2, "unidad"))),
                "Armenia", "Calle 1 # 2-3", "Juan Pérez");
    }

    @Test
    void daDeBajaUnaPresentacionSinCompras() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        CompraRepositorioMemoria compras = new CompraRepositorioMemoria();
        publicarMerchandising(presentaciones, "pres-1");
        EliminarPresentacion eliminar = new EliminarPresentacion(presentaciones, compras);

        Presentacion eliminada = eliminar.ejecutar("pres-1");

        assertTrue(eliminada.isEliminada());
        assertTrue(presentaciones.buscarPorId("pres-1").get().isEliminada());
    }

    @Test
    void noSePuedeEliminarUnaPresentacionConUnaCompraActiva() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        CompraRepositorioMemoria compras = new CompraRepositorioMemoria();
        publicarMerchandising(presentaciones, "pres-2");
        comprar(presentaciones, compras, "pres-2");
        EliminarPresentacion eliminar = new EliminarPresentacion(presentaciones, compras);

        assertThrows(ReglaDominioException.class, () -> eliminar.ejecutar("pres-2"));
        assertFalse(presentaciones.buscarPorId("pres-2").get().isEliminada());
    }

    @Test
    void unaCompraDeOtraPresentacionNoBloqueaLaEliminacion() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        CompraRepositorioMemoria compras = new CompraRepositorioMemoria();
        publicarMerchandising(presentaciones, "pres-3");
        publicarMerchandising(presentaciones, "pres-4");
        comprar(presentaciones, compras, "pres-4");
        EliminarPresentacion eliminar = new EliminarPresentacion(presentaciones, compras);

        Presentacion eliminada = eliminar.ejecutar("pres-3");

        assertTrue(eliminada.isEliminada());
    }

    @Test
    void unaCompraDigitalActivaNoBloqueaLaEliminacion() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        CompraRepositorioMemoria compras = new CompraRepositorioMemoria();
        publicarMerchandising(presentaciones, "pres-6");
        compras.guardar(CompraDigital.iniciar("comprador-1", "pres-6", new Precio(50000, "COP")));
        EliminarPresentacion eliminar = new EliminarPresentacion(presentaciones, compras);

        assertTrue(eliminar.ejecutar("pres-6").isEliminada());
    }

    @Test
    void lanzaExcepcionSiLaPresentacionNoExiste() {
        EliminarPresentacion eliminar = new EliminarPresentacion(new PresentacionRepositorioMemoria(),
                new CompraRepositorioMemoria());

        assertThrows(ReglaDominioException.class, () -> eliminar.ejecutar("no-existe"));
    }

    @Test
    void unaPresentacionEliminadaYaNoSePuedeComprar() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        CompraRepositorioMemoria compras = new CompraRepositorioMemoria();
        publicarMerchandising(presentaciones, "pres-5");
        new EliminarPresentacion(presentaciones, compras).ejecutar("pres-5");

        assertThrows(ReglaDominioException.class, () -> comprar(presentaciones, compras, "pres-5"));
    }
}
