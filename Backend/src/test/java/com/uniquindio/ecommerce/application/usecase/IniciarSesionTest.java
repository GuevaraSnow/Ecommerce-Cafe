package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.CredencialesInvalidasException;
import com.uniquindio.ecommerce.Domain.service.ServicioDeAutenticacion;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** El caso de uso solo delega en el servicio de autenticación y deja pasar su excepción. */
@ExtendWith(MockitoExtension.class)
class IniciarSesionTest {

    private static final Email EMAIL = new Email("joseph@uniquindio.edu.co");
    private static final Contrasena CONTRASENA = new Contrasena("ClaveCorrecta123");

    @Mock
    private ServicioDeAutenticacion servicioDeAutenticacion;

    @InjectMocks
    private IniciarSesion iniciarSesion;

    @Test
    void devuelveAlUsuarioAutenticado() {
        Usuario usuario = Usuario.registrar("usr-1", "Joseph Cortes", EMAIL, new Contrasena("hash"),
                new Telefono("3001234567"), new FechaDeNacimiento(LocalDate.of(2000, 1, 1)), Rol.CAFICULTOR);
        when(servicioDeAutenticacion.autenticar(EMAIL, CONTRASENA)).thenReturn(usuario);

        assertSame(usuario, iniciarSesion.ejecutar(EMAIL, CONTRASENA));
    }

    @Test
    void siElServicioRechazaLasCredencialesLaExcepcionLlegaTalCual() {
        when(servicioDeAutenticacion.autenticar(EMAIL, CONTRASENA)).thenThrow(new CredencialesInvalidasException());

        assertThrows(CredencialesInvalidasException.class, () -> iniciarSesion.ejecutar(EMAIL, CONTRASENA));
    }
}
