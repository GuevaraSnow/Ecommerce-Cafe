package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActualizarPresentacionTest {

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/manilla.png", true)));
    }

    private ArticuloDeMerchandising publicarMerchandising(PresentacionRepositorioMemoria repositorio, String id) {
        PublicarArticuloMerchandising publicar = new PublicarArticuloMerchandising(repositorio);
        return publicar.ejecutar(id, "vendedor-1", "Manilla de semilla de café", new Precio(15000, "COP"),
                new Cantidad(20, "unidad"), galeriaValida(), "semilla de café e hilo encerado",
                "Manilla artesanal", RolVendedor.VENDEDOR_DERIVADOS);
    }

    private Presentacion publicarTrazable(PresentacionRepositorioMemoria repositorio, String id) {
        Presentacion presentacion = Presentacion.publicar(id, "vendedor-1", "lote-1", null,
                "Café Verde Finca La Esperanza", TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"),
                new Cantidad(200, "kg"), galeriaValida(), null, RolVendedor.CAFICULTOR, null, null, null);
        repositorio.guardar(presentacion);
        return presentacion;
    }

    @Test
    void actualizaElPrecio() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarMerchandising(repositorio, "pres-1");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);

        Presentacion actualizada = actualizar.ejecutar("pres-1", new Precio(18000, "COP"), null, null, null, null);

        assertEquals(new Precio(18000, "COP"), actualizada.getPrecio());
    }

    @Test
    void aumentarLaCantidadTotalRepone() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarMerchandising(repositorio, "pres-2");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);

        Presentacion actualizada = actualizar.ejecutar("pres-2", null, new Cantidad(35, "unidad"), null, null, null);

        assertEquals(35, actualizada.getCantidadDisponible().valor());
    }

    @Test
    void reducirLaCantidadTotalDescuenta() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarMerchandising(repositorio, "pres-3");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);

        Presentacion actualizada = actualizar.ejecutar("pres-3", null, new Cantidad(5, "unidad"), null, null, null);

        assertEquals(5, actualizada.getCantidadDisponible().valor());
    }

    @Test
    void desactivarYReactivarCambianElEstado() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarMerchandising(repositorio, "pres-4");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);

        Presentacion pausada = actualizar.ejecutar("pres-4", null, null, false, null, null);
        assertEquals(EstadoDePublicacion.INACTIVA, pausada.getEstado());

        Presentacion reactivada = actualizar.ejecutar("pres-4", null, null, true, null, null);
        assertEquals(EstadoDePublicacion.ACTIVA, reactivada.getEstado());
    }

    @Test
    void noSePuedeActivarUnaPresentacionQueNoEstaInactiva() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarMerchandising(repositorio, "pres-5");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);

        assertThrows(ReglaDominioException.class, () -> actualizar.ejecutar("pres-5", null, null, true, null, null));
    }

    @Test
    void lanzaExcepcionSiLaPresentacionNoExiste() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);

        assertThrows(ReglaDominioException.class,
                () -> actualizar.ejecutar("no-existe", new Precio(1000, "COP"), null, null, null, null));
    }

    @Test
    void reemplazaLaGaleria() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarMerchandising(repositorio, "pres-6");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);
        Galeria nuevaGaleria = new Galeria(List.of(new ImagenDePresentacion("http://img/manilla-nueva.png", true)));

        Presentacion actualizada = actualizar.ejecutar("pres-6", null, null, null, nuevaGaleria, null);

        assertEquals(nuevaGaleria, actualizada.getGaleria());
    }

    @Test
    void registraNotaDeCataEnUnaPresentacionTrazable() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarTrazable(repositorio, "pres-7");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);
        NotaDeCata nota = new NotaDeCata(List.of("Chocolate", "Panela"));

        actualizar.ejecutar("pres-7", null, null, null, null, nota);

        var trazable = (com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable) repositorio.buscarPorId("pres-7").get();
        assertEquals(nota, trazable.getNotaDeCata());
    }

    @Test
    void noSePuedeRegistrarNotaDeCataEnMerchandising() {
        PresentacionRepositorioMemoria repositorio = new PresentacionRepositorioMemoria();
        publicarMerchandising(repositorio, "pres-8");
        ActualizarPresentacion actualizar = new ActualizarPresentacion(repositorio);
        NotaDeCata nota = new NotaDeCata(List.of("Chocolate"));

        assertThrows(ReglaDominioException.class, () -> actualizar.ejecutar("pres-8", null, null, null, null, nota));
    }
}
