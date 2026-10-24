package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.TransformacionRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.UsuarioRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicioDePublicacionTest {

    private final TransformacionRepositorioMemoria transformaciones = new TransformacionRepositorioMemoria();
    private final UsuarioRepositorioMemoria usuarios = new UsuarioRepositorioMemoria();
    private final ServicioDePublicacion servicio =
            new ServicioDePublicacion(new LoteRepositorioMemoria(), transformaciones, usuarios);

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/cafe.png", true)));
    }

    private void registrarUsuario(String id, Rol rol) {
        usuarios.guardar(Usuario.registrar(
                id, "Usuario Vendedor", new Email("vendedor@uniquindio.edu.co"),
                new Contrasena("Password123!"), new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(1990, 1, 1)), rol));
    }

    private void registrarTransformacion(String id, PerfilTueste perfil) {
        transformaciones.guardar(Transformacion.registrar(id, List.of("LOT-2026-001"), new Cantidad(50, "kg"),
                new PorcentajeDeMerma(0.18), "tostador-1", LocalDate.now(), perfil));
    }

    private Cantidad disponibleEn(String transformacionId) {
        return transformaciones.buscarPorId(transformacionId).get().getCantidadResultanteDisponible();
    }

    @Test
    void publicarCafeTostadoConElPerfilDeLaTransformacionDescuentaDeElla() {
        registrarUsuario("vendedor-1", Rol.TOSTADOR);
        registrarTransformacion("t-1", PerfilTueste.MEDIO); // resultante = 41 kg

        Presentacion presentacion = servicio.publicar("pres-1", "vendedor-1", null, "t-1", "Café Medio",
                TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(20, "kg"), galeriaValida(),
                PerfilTueste.MEDIO, Rol.TOSTADOR, new FechaDeTueste(LocalDate.now()));

        assertInstanceOf(PresentacionTrazable.class, presentacion);
        assertEquals(21, disponibleEn("t-1").valor(), 0.0001);
    }

    @Test
    void noSePuedePublicarConUnPerfilDistintoAlDeLaTransformacionYNadaCambia() {
        registrarUsuario("vendedor-1", Rol.TOSTADOR);
        registrarTransformacion("t-2", PerfilTueste.MEDIO);
        Cantidad antes = disponibleEn("t-2");

        assertThrows(ReglaDominioException.class, () -> servicio.publicar("pres-2", "vendedor-1", null, "t-2",
                "Café Oscuro", TipoDePresentacion.CAFE_TOSTADO, new Precio(32000, "COP"), new Cantidad(10, "kg"),
                galeriaValida(), PerfilTueste.OSCURO, Rol.TOSTADOR, new FechaDeTueste(LocalDate.now())));

        assertEquals(antes, disponibleEn("t-2"));
    }

    @Test
    void unDerivadoConsumibleSinPerfilSePublicaDesdeCualquierTransformacion() {
        registrarUsuario("vendedor-1", Rol.VENDEDOR_DERIVADOS);
        registrarTransformacion("t-3", PerfilTueste.OSCURO);

        Presentacion presentacion = servicio.publicar("pres-3", "vendedor-1", null, "t-3", "Licor de café",
                TipoDePresentacion.DERIVADO_CONSUMIBLE, new Precio(45000, "COP"), new Cantidad(5, "kg"),
                galeriaValida(), null, Rol.VENDEDOR_DERIVADOS, null);

        assertInstanceOf(PresentacionTrazable.class, presentacion);
        assertEquals(36, disponibleEn("t-3").valor(), 0.0001);
    }
}
