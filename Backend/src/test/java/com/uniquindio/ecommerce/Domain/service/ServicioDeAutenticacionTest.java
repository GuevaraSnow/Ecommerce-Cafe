package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.CredencialesInvalidasException;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Un correo inexistente, una contraseña errada y un usuario dado de baja fallan con la misma excepción y mensaje. */
@ExtendWith(MockitoExtension.class)
class ServicioDeAutenticacionTest {

    private static final Email EMAIL = new Email("joseph@uniquindio.edu.co");

    @Mock
    private UsuarioRepositorio usuarioRepositorio;
    @Mock
    private EncriptadorContrasenaRepositorio encriptador;

    private ServicioDeAutenticacion servicio;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        servicio = new ServicioDeAutenticacion(usuarioRepositorio, encriptador);
        usuario = Usuario.registrar("usr-1", "Joseph Cortes", EMAIL, new Contrasena("hashGuardado"),
                new Telefono("3001234567"), new FechaDeNacimiento(LocalDate.of(2000, 1, 1)), Rol.CAFICULTOR);
    }

    @Test
    void conElCorreoYLaContrasenaCorrectosDevuelveAlUsuario() {
        when(usuarioRepositorio.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(encriptador.coinciden("ClaveCorrecta123", "hashGuardado")).thenReturn(true);

        assertSame(usuario, servicio.autenticar(EMAIL, new Contrasena("ClaveCorrecta123")));
    }

    @Test
    void conUnCorreoQueNoExisteLanzaCredencialesInvalidas() {
        when(usuarioRepositorio.buscarPorEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticar(EMAIL, new Contrasena("ClaveCorrecta123")));
    }

    @Test
    void conUnaContrasenaIncorrectaLanzaCredencialesInvalidas() {
        when(usuarioRepositorio.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(encriptador.coinciden("Equivocada123", "hashGuardado")).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticar(EMAIL, new Contrasena("Equivocada123")));
    }

    @Test
    void conUnUsuarioDadoDeBajaLanzaCredencialesInvalidasSinComprobarLaContrasena() {
        usuario.eliminarLogicamente();
        when(usuarioRepositorio.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticar(EMAIL, new Contrasena("ClaveCorrecta123")));

        verify(encriptador, never()).coinciden(any(), any());
    }

    @Test
    void lasTresCausasDeFalloDanElMismoMensaje() {
        Usuario dadoDeBaja = Usuario.registrar("usr-2", "Ana", new Email("ana@correo.com"), new Contrasena("hash2"),
                new Telefono("3011112233"), new FechaDeNacimiento(LocalDate.of(1998, 3, 14)), Rol.COMPRADOR);
        dadoDeBaja.eliminarLogicamente();
        when(usuarioRepositorio.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario)).thenReturn(Optional.empty());
        when(usuarioRepositorio.buscarPorEmail(new Email("ana@correo.com"))).thenReturn(Optional.of(dadoDeBaja));
        when(encriptador.coinciden(any(), any())).thenReturn(false);

        String contrasenaErrada = mensajeAlAutenticar(EMAIL);
        String correoInexistente = mensajeAlAutenticar(EMAIL);
        String usuarioDeBaja = mensajeAlAutenticar(new Email("ana@correo.com"));

        assertEquals(Set.of(CredencialesInvalidasException.MENSAJE),
                new HashSet<>(List.of(contrasenaErrada, correoInexistente, usuarioDeBaja)));
    }

    private String mensajeAlAutenticar(Email email) {
        return assertThrows(CredencialesInvalidasException.class,
                () -> servicio.autenticar(email, new Contrasena("Cualquiera123"))).getMessage();
    }
}
