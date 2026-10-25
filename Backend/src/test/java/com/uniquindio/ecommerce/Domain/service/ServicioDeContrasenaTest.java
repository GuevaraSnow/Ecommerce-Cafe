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
}