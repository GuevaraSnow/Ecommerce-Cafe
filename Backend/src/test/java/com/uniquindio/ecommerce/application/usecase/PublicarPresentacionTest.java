package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.PresentacionRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.TransformacionRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PublicarPresentacionTest {

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/cafe.png", true)));
    }

    private Transformacion registrarTransformacion(TransformacionRepositorioMemoria repositorio, String id,
                                                     double cantidadEntradaKg) {
        Transformacion transformacion = Transformacion.registrar(id, List.of("lote-1"),
                new Cantidad(cantidadEntradaKg, "kg"), new PorcentajeDeMerma(0.18), "tostador-1", LocalDate.now(),
                PerfilTueste.MEDIO);
        repositorio.guardar(transformacion);
        return transformacion;
    }

    @Test
    void publicaCafeTostadoYDescuentaLaCantidadResultanteDisponible() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
        registrarTransformacion(transformaciones, "transformacion-1", 50); // resultante = 41 kg (18% merma)
        PublicarPresentacion publicar = new PublicarPresentacion(presentaciones, transformaciones);

        Presentacion presentacion = publicar.ejecutar("pres-1", "vendedor-1", null, "transformacion-1",
                "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(20, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now()));

        assertTrue(presentacion instanceof PresentacionTrazable);
        assertEquals(21, transformaciones.buscarPorId("transformacion-1").get()
                .getCantidadResultanteDisponible().valor(), 0.0001);
        assertTrue(presentaciones.buscarPorId("pres-1").isPresent());
    }

    @Test
    void noSePuedePublicarMasCantidadDeLaResultanteDisponible() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
        registrarTransformacion(transformaciones, "transformacion-2", 50); // resultante = 41 kg

        PublicarPresentacion publicar = new PublicarPresentacion(presentaciones, transformaciones);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-2", "vendedor-1", null,
                "transformacion-2", "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(50, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now())));
    }

    @Test
    void publicarDosVecesDeLaMismaTransformacionAcumulaElDescuento() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
        registrarTransformacion(transformaciones, "transformacion-3", 50); // resultante = 41 kg
        PublicarPresentacion publicar = new PublicarPresentacion(presentaciones, transformaciones);

        publicar.ejecutar("pres-3a", "vendedor-1", null, "transformacion-3", "Café Tostado Medio",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(30, "kg"), galeriaValida(),
                PerfilTueste.MEDIO, RolVendedor.TOSTADOR, new FechaDeTueste(LocalDate.now()));

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-3b", "vendedor-1", null,
                "transformacion-3", "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(15, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now())));
    }

    @Test
    void publicaCafeVerdeDesdeUnLoteSinTocarTransformaciones() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
        PublicarPresentacion publicar = new PublicarPresentacion(presentaciones, transformaciones);

        Presentacion presentacion = publicar.ejecutar("pres-4", "vendedor-1", "lote-1", null, "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeriaValida(),
                null, RolVendedor.CAFICULTOR, null);

        assertTrue(presentacion instanceof PresentacionTrazable);
        assertTrue(presentaciones.buscarPorId("pres-4").isPresent());
    }

    @Test
    void noSePuedePublicarSiLaTransformacionNoExiste() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
        PublicarPresentacion publicar = new PublicarPresentacion(presentaciones, transformaciones);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-5", "vendedor-1", null,
                "no-existe", "Café Tostado Medio", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"),
                new Cantidad(10, "kg"), galeriaValida(), PerfilTueste.MEDIO, RolVendedor.TOSTADOR,
                new FechaDeTueste(LocalDate.now())));
    }

    @Test
    void noSePuedeUsarEsteCasoDeUsoParaMerchandising() {
        PresentacionRepositorioMemoria presentaciones = new PresentacionRepositorioMemoria();
        TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
        PublicarPresentacion publicar = new PublicarPresentacion(presentaciones, transformaciones);

        assertThrows(ReglaDominioException.class, () -> publicar.ejecutar("pres-6", "vendedor-1", null, null,
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, RolVendedor.VENDEDOR_DERIVADOS, null));
    }
}
