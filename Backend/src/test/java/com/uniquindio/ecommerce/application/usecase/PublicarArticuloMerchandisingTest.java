package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PublicarArticuloMerchandisingTest {

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/manilla.png", true)));
    }

    @Test
    void unVendedorDeDerivadosPublicaUnArticuloDeMerchandising() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        PublicarArticuloMerchandising publicar = new PublicarArticuloMerchandising(repositorio);

        ArticuloDeMerchandising articulo = publicar.ejecutar(
                "pres-1",
                "vendedor-1",
                "Manilla de semilla de café",
                new Precio(15000, "COP"),
                new Cantidad(20, "unidad"),
                galeriaValida(),
                "semilla de café e hilo encerado",
                "Manilla artesanal hecha con semillas de café",
                RolVendedor.VENDEDOR_DERIVADOS);

        assertEquals("Manilla artesanal hecha con semillas de café", articulo.getDescripcion());
        assertEquals("semilla de café e hilo encerado", articulo.getMaterial());
        assertTrue(repositorio.buscarPorId("pres-1").isPresent());
    }

    @Test
    void unCaficultorNoPuedePublicarMerchandising() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        PublicarArticuloMerchandising publicar = new PublicarArticuloMerchandising(repositorio);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar(
                "pres-2",
                "vendedor-1",
                "Manilla de semilla de café",
                new Precio(15000, "COP"),
                new Cantidad(20, "unidad"),
                galeriaValida(),
                "semilla de café e hilo encerado",
                "Manilla artesanal hecha con semillas de café",
                RolVendedor.CAFICULTOR));
    }

    @Test
    void noSePuedePublicarSinMaterial() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        PublicarArticuloMerchandising publicar = new PublicarArticuloMerchandising(repositorio);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar(
                "pres-3",
                "vendedor-1",
                "Manilla de semilla de café",
                new Precio(15000, "COP"),
                new Cantidad(20, "unidad"),
                galeriaValida(),
                "  ",
                "Manilla artesanal hecha con semillas de café",
                RolVendedor.VENDEDOR_DERIVADOS));
    }
}
