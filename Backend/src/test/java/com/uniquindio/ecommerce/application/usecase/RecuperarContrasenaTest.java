package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.NotificadorDeCorreo;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeNacimiento;
import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperarContrasenaTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private NotificadorDeCorreo notificadorDeCorreo;

    @InjectMocks
    private RecuperarContrasena recuperarContrasena;

    @Test
    void recuperarContrasenaEnviaCorreoUnaSolaVezYDevuelveVoid() {
        String correoText = "joseph@uniquindio.edu.co";
        Email email = new Email(correoText);

        Usuario usuario = Usuario.registrar(
                "usr-1",
                "Joseph Cortes",
                email,
                new Contrasena("Password123!"),
                new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(2000, 1, 1)),
                Rol.CAFICULTOR
        );

        when(usuarioRepositorio.buscarPorEmail(any(Email.class))).thenReturn(Optional.of(usuario));

        recuperarContrasena.ejecutar(email);

        verify(usuarioRepositorio, times(1)).guardar(usuario);
        verify(notificadorDeCorreo, times(1)).enviarCodigoDeRecuperacion(eq(email), anyString());
    }
}