package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.Domain.service.ServicioDeContrasena;
import com.uniquindio.ecommerce.infrastructure.persistence.EncriptadorBCryptMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.NotificadorCorreoMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.UsuarioRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RestablecerContrasenaTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.registrar(
                "usr-1",
                "Joseph Cortes",
                new Email("joseph@uniquindio.edu.co"),
                new Contrasena("PassOriginal123!"),
                new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(2000, 1, 1)),
                Rol.CAFICULTOR
        );
    }

    @Test
    void codigoCorrectoCambiaContrasenaYBorraElCodigo() {
        CodigoDeRecuperacion codigo = new CodigoDeRecuperacion("123456", LocalDateTime.now().plusMinutes(15));
        usuario.asignarCodigoRecuperacion(codigo);

        Contrasena nuevaPass = new Contrasena("NuevaPass123!");
        usuario.cambiarContrasena("123456", nuevaPass);

        assertEquals("NuevaPass123!", usuario.getContrasena().contrasena());
        assertNull(usuario.getCodigoRecuperacion());
    }

    @Test
    void codigoIncorrectoLanzaExcepcionYNoCambiaContrasena() {
        CodigoDeRecuperacion codigo = new CodigoDeRecuperacion("123456", LocalDateTime.now().plusMinutes(15));
        usuario.asignarCodigoRecuperacion(codigo);

        Contrasena nuevaPass = new Contrasena("NuevaPass123!");

        assertThrows(ReglaDominioException.class, () ->
                usuario.cambiarContrasena("654321", nuevaPass)
        );

        assertEquals("PassOriginal123!", usuario.getContrasena().contrasena());
        assertNotNull(usuario.getCodigoRecuperacion());
    }

    @Test
    void codigoVencidoLanzaExcepcionYNoCambiaContrasena() {
        CodigoDeRecuperacion codigoVencido = new CodigoDeRecuperacion("123456", LocalDateTime.now().minusMinutes(1));
        usuario.asignarCodigoRecuperacion(codigoVencido);

        Contrasena nuevaPass = new Contrasena("NuevaPass123!");

        assertThrows(ReglaDominioException.class, () ->
                usuario.cambiarContrasena("123456", nuevaPass)
        );

        assertEquals("PassOriginal123!", usuario.getContrasena().contrasena());
    }

    // ----- el flujo completo, con repositorio en memoria y el encriptador BCrypt real -----

    private static final Email EMAIL = new Email("joseph@uniquindio.edu.co");

    private final UsuarioRepositorioMemoria repositorio = new UsuarioRepositorioMemoria();
    private final EncriptadorBCryptMemoria encriptador = new EncriptadorBCryptMemoria();
    private final RecuperarContrasena recuperar = new RecuperarContrasena(repositorio, new NotificadorCorreoMemoria());
    private final RestablecerContrasena restablecer =
            new RestablecerContrasena(repositorio, new ServicioDeContrasena(encriptador));
    private final IniciarSesion iniciarSesion = new IniciarSesion(repositorio, encriptador);

    /** Registra al usuario como lo deja el registro real: con la contraseña ya encriptada. */
    private void registrarConContrasena(String contrasenaPlana) {
        repositorio.guardar(Usuario.registrar("usr-1", "Joseph Cortes", EMAIL,
                new Contrasena(encriptador.encriptar(contrasenaPlana)), new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(2000, 1, 1)), Rol.CAFICULTOR));
    }

    private String codigoAsignado() {
        return repositorio.buscarPorEmail(EMAIL).orElseThrow().getCodigoRecuperacion().codigo();
    }

    @Test
    void despuesDeRestablecerSePuedeIniciarSesionConLaContrasenaNuevaYYaNoConLaVieja() {
        registrarConContrasena("PassOriginal123!");
        recuperar.ejecutar(EMAIL);

        restablecer.ejecutar(EMAIL, codigoAsignado(), new ContrasenaPlana("ClaveNueva123"));

        assertEquals("usr-1", iniciarSesion.ejecutar(EMAIL, new Contrasena("ClaveNueva123")).getId());
        assertThrows(ReglaDominioException.class,
                () -> iniciarSesion.ejecutar(EMAIL, new Contrasena("PassOriginal123!")));
        assertNull(repositorio.buscarPorEmail(EMAIL).orElseThrow().getCodigoRecuperacion());
    }

    @Test
    void conUnCodigoIncorrectoLaContrasenaNoCambiaYSeSigueEntrandoConLaVieja() {
        registrarConContrasena("PassOriginal123!");
        recuperar.ejecutar(EMAIL);
        ContrasenaPlana nueva = new ContrasenaPlana("ClaveNueva123");

        assertThrows(ReglaDominioException.class, () -> restablecer.ejecutar(EMAIL, "000000", nueva));

        assertEquals("usr-1", iniciarSesion.ejecutar(EMAIL, new Contrasena("PassOriginal123!")).getId());
        assertThrows(ReglaDominioException.class, () -> iniciarSesion.ejecutar(EMAIL, new Contrasena("ClaveNueva123")));
        assertNotNull(repositorio.buscarPorEmail(EMAIL).orElseThrow().getCodigoRecuperacion());
    }
}
