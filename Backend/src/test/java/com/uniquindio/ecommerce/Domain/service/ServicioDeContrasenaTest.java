package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioDeContrasenaTest {

    @Mock
    private EncriptadorContrasenaRepositorio encriptador;

    private ServicioDeContrasena servicio;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        servicio = new ServicioDeContrasena(encriptador);
        usuario = Usuario.registrar(
                "usr-1",
                "Joseph Cortes",
                new Email("joseph@uniquindio.edu.co"),
                new Contrasena("hashActual123"),
                new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(2000, 1, 1)),
                Rol.CAFICULTOR
        );
    }

    @Test
    void cambiarContrasenaConClaveActualCorrectaActualizaElHash() {
        when(encriptador.coinciden("ClaveActual123!", "hashActual123")).thenReturn(true);
        when(encriptador.encriptar("NuevaClave123!")).thenReturn("nuevoHash456");

        ContrasenaPlana nuevaContrasenaPlana = new ContrasenaPlana("NuevaClave123!");
        servicio.cambiarContrasena(usuario, "ClaveActual123!", nuevaContrasenaPlana);

        assertEquals("nuevoHash456", usuario.getContrasena().contrasena());
    }

    @Test
    void cambiarContrasenaConClaveActualIncorrectaLanzaExcepcionYNoCambiaHash() {
        when(encriptador.coinciden("MalaPassword123!", "hashActual123")).thenReturn(false);

        ContrasenaPlana nuevaContrasenaPlana = new ContrasenaPlana("NuevaClave123!");

        assertThrows(ReglaDominioException.class, () ->
                servicio.cambiarContrasena(usuario, "MalaPassword123!", nuevaContrasenaPlana)
        );

        assertEquals("hashActual123", usuario.getContrasena().contrasena());
    }

    @Test
    void cambiarContrasenaConParametrosNulosOVaciosLanzaExcepcion() {
        ContrasenaPlana nuevaContrasenaPlana = new ContrasenaPlana("NuevaClave123!");

        assertThrows(ReglaDominioException.class, () ->
                servicio.cambiarContrasena(usuario, null, nuevaContrasenaPlana)
        );
        assertThrows(ReglaDominioException.class, () ->
                servicio.cambiarContrasena(usuario, "   ", nuevaContrasenaPlana)
        );
        assertThrows(ReglaDominioException.class, () ->
                servicio.cambiarContrasena(usuario, "ClaveActual123!", null)
        );
        assertThrows(ReglaDominioException.class, () ->
                servicio.cambiarContrasena(null, "ClaveActual123!", nuevaContrasenaPlana)
        );
    }

    // ----- restablecer con el código de recuperación -----

    @Test
    void restablecerContrasenaGuardaLaContrasenaNuevaEncriptadaYDescartaElCodigo() {
        usuario.asignarCodigoRecuperacion(new CodigoDeRecuperacion("123456", LocalDateTime.now().plusMinutes(15)));
        when(encriptador.encriptar("NuevaClave123")).thenReturn("hashNuevo");

        servicio.restablecerContrasena(usuario, "123456", new ContrasenaPlana("NuevaClave123"));

        assertEquals("hashNuevo", usuario.getContrasena().contrasena());
        assertNull(usuario.getCodigoRecuperacion());
    }

    @Test
    void restablecerContrasenaConUnCodigoIncorrectoLanzaExcepcionYNoCambiaElUsuario() {
        usuario.asignarCodigoRecuperacion(new CodigoDeRecuperacion("123456", LocalDateTime.now().plusMinutes(15)));
        when(encriptador.encriptar("NuevaClave123")).thenReturn("hashNuevo");

        assertThrows(ReglaDominioException.class, () ->
                servicio.restablecerContrasena(usuario, "654321", new ContrasenaPlana("NuevaClave123")));

        assertEquals("hashActual123", usuario.getContrasena().contrasena());
        assertNotNull(usuario.getCodigoRecuperacion());
    }

    @Test
    void restablecerContrasenaConUnCodigoVencidoLanzaExcepcionYNoCambiaElUsuario() {
        usuario.asignarCodigoRecuperacion(new CodigoDeRecuperacion("123456", LocalDateTime.now().minusMinutes(1)));
        when(encriptador.encriptar("NuevaClave123")).thenReturn("hashNuevo");

        assertThrows(ReglaDominioException.class, () ->
                servicio.restablecerContrasena(usuario, "123456", new ContrasenaPlana("NuevaClave123")));

        assertEquals("hashActual123", usuario.getContrasena().contrasena());
    }

    @Test
    void restablecerContrasenaConDatosNulosLanzaExcepcion() {
        assertThrows(ReglaDominioException.class, () ->
                servicio.restablecerContrasena(null, "123456", new ContrasenaPlana("NuevaClave123")));
        assertThrows(ReglaDominioException.class, () ->
                servicio.restablecerContrasena(usuario, "123456", null));
    }
}
