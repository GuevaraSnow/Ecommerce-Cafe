package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.CompraRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioDeBajaDePresentacionTest {

    private final CompraRepositorioMemoria compras = new CompraRepositorioMemoria();
    private final ServicioDeBajaDePresentacion servicio = new ServicioDeBajaDePresentacion(compras);

    private Presentacion publicarMerchandising(String id) {
        return Presentacion.publicar(id, "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/manilla.png", true))), null,
                RolVendedor.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    private Compra compraPendienteCon(String presentacionId) {
        Compra compra = Compra.abrirCarrito("compra-" + presentacionId, "comprador-1");
        compra.agregarPresentacion(presentacionId, new Precio(15000, "COP"), new Cantidad(2, "unidad"));
        compras.guardar(compra);
        return compra;
    }

    @Test
    void daDeBajaUnaPresentacionSinCompras() {
        Presentacion presentacion = publicarMerchandising("pres-1");

        servicio.darDeBaja(presentacion);

        assertTrue(presentacion.isEliminada());
    }

    @Test
    void noSePuedeDarDeBajaUnaPresentacionConUnaCompraActivaYNoCambia() {
        Presentacion presentacion = publicarMerchandising("pres-2");
        compraPendienteCon("pres-2");

        assertThrows(ReglaDominioException.class, () -> servicio.darDeBaja(presentacion));

        assertFalse(presentacion.isEliminada());
    }

    @Test
    void unaCompraDeOtraPresentacionNoBloqueaLaBaja() {
        Presentacion presentacion = publicarMerchandising("pres-3");
        compraPendienteCon("pres-4");

        servicio.darDeBaja(presentacion);

        assertTrue(presentacion.isEliminada());
    }

    @Test
    void unaCompraCanceladaYaNoBloqueaLaBaja() {
        Presentacion presentacion = publicarMerchandising("pres-5");
        Compra compra = compraPendienteCon("pres-5");
        compra.cancelar();
        compras.guardar(compra);

        servicio.darDeBaja(presentacion);

        assertTrue(presentacion.isEliminada());
    }
}
