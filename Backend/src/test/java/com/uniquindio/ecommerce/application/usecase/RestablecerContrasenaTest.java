package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
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

    @Test
    void flujoCompletoRecuperarYRestablecerContrasenaEnMemoria() {
        UsuarioRepositorioMemoria repositorioMemoria = new UsuarioRepositorioMemoria();
        repositorioMemoria.guardar(usuario);

        // Simulamos asignación de código en la recuperación
        CodigoDeRecuperacion codigoGenerado = CodigoDeRecuperacion.generar();
        usuario.asignarCodigoRecuperacion(codigoGenerado);
        repositorioMemoria.guardar(usuario);

        // Ejecución del restablecimiento
        RestablecerContrasena casoDeUso = new RestablecerContrasena(repositorioMemoria);
        com.uniquindio.ecommerce.application.dto.request.RestablecerContrasenaRequest request =
                new com.uniquindio.ecommerce.application.dto.request.RestablecerContrasenaRequest(
                        "joseph@uniquindio.edu.co",
                        codigoGenerado.codigo(),
                        "ClaveRestablecida123!"
                );

        casoDeUso.ejecutar(request);

        Usuario usuarioActualizado = repositorioMemoria.buscarPorEmail(new Email("joseph@uniquindio.edu.co")).orElseThrow();
        assertEquals("ClaveRestablecida123!", usuarioActualizado.getContrasena().contrasena());
        assertNull(usuarioActualizado.getCodigoRecuperacion());
    }
}