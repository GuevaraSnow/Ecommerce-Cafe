package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El caso de uso carga al usuario, le aplica el cambio y lo guarda; si el nombre es inválido, nada cambia ni se guarda. */
@ExtendWith(MockitoExtension.class)
class EditarPerfilTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @InjectMocks
    private EditarPerfil editarPerfil;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.registrar("usr-1", "Joseph Cortes", new Email("joseph@uniquindio.edu.co"),
                new Contrasena("hash"), new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(2000, 1, 1)), Rol.CAFICULTOR);
    }

    @Test
    void cambiaElNombreYElTelefonoYGuarda() {
        when(usuarioRepositorio.buscarPorId("usr-1")).thenReturn(Optional.of(usuario));

        Usuario editado = editarPerfil.ejecutar("usr-1", "  Joseph Editado  ", new Telefono("3119876543"));

        assertEquals("Joseph Editado", editado.getNombre());
        assertEquals("3119876543", editado.getTelefono().telefono());
        verify(usuarioRepositorio).guardar(usuario);
    }

    @Test
    void sinTelefonoSeQuitaElTelefono() {
        when(usuarioRepositorio.buscarPorId("usr-1")).thenReturn(Optional.of(usuario));

        editarPerfil.ejecutar("usr-1", "Joseph Cortes", null);

        assertNull(usuario.getTelefono());
    }

    @Test
    void conUnNombreVacioLanzaExcepcionYNoCambiaNiGuardaAlUsuario() {
        when(usuarioRepositorio.buscarPorId("usr-1")).thenReturn(Optional.of(usuario));

        assertThrows(ReglaDominioException.class, () -> editarPerfil.ejecutar("usr-1", "   ", new Telefono("3119876543")));

        assertEquals("Joseph Cortes", usuario.getNombre());
        assertEquals("3001234567", usuario.getTelefono().telefono());
        verify(usuarioRepositorio, never()).guardar(any());
    }

    @Test
    void unUsuarioInexistenteLanzaNoEncontrado() {
        when(usuarioRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> editarPerfil.ejecutar("no-existe", "Nombre", null));
    }
}
