package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeNacimiento;
import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private final Email emailValido = new Email("joseph@uniquindio.edu.co");
    private final Contrasena contrasenaValida = new Contrasena("Password123!");
    private final Telefono telefonoValido = new Telefono("3001234567");
    private final FechaDeNacimiento fechaValida = new FechaDeNacimiento(LocalDate.of(2000, 1, 1));

    @Test
    void registrarUsuarioConDatosValidosExitoso() {
        Usuario usuario = Usuario.registrar(
                "usr-1", "Joseph Cortés", emailValido, contrasenaValida,
                telefonoValido, fechaValida, Rol.CAFICULTOR
        );

        assertNotNull(usuario);
        assertEquals("usr-1", usuario.getId());
        assertEquals("Joseph Cortés", usuario.getNombre());
        assertEquals(Rol.CAFICULTOR, usuario.getRol());
        assertEquals("Password123!", usuario.getContrasena().contrasena());
    }

    @Test
    void registrarUsuarioConIdVacioLanzaExcepcion() {
        assertThrows(ReglaDominioException.class, () ->
                Usuario.registrar("", "Joseph Cortés", emailValido, contrasenaValida, telefonoValido, fechaValida, Rol.COMPRADOR)
        );
    }

    @Test
    void registrarUsuarioConNombreVacioLanzaExcepcion() {
        assertThrows(ReglaDominioException.class, () ->
                Usuario.registrar("usr-1", "   ", emailValido, contrasenaValida, telefonoValido, fechaValida, Rol.COMPRADOR)
        );
    }

    @Test
    void registrarUsuarioConEmailNuloLanzaExcepcion() {
        assertThrows(ReglaDominioException.class, () ->
                Usuario.registrar("usr-1", "Joseph Cortés", null, contrasenaValida, telefonoValido, fechaValida, Rol.COMPRADOR)
        );
    }

    @Test
    void registrarUsuarioConRolNuloLanzaExcepcion() {
        assertThrows(ReglaDominioException.class, () ->
                Usuario.registrar("usr-1", "Joseph Cortés", emailValido, contrasenaValida, telefonoValido, fechaValida, null)
        );
    }
}