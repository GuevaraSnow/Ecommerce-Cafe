package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.RolVendedor;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VendedorTest {

    private Vendedor vendedorValido(String nombre) {
        return Vendedor.registrar(
                nombre,
                new Email("caficultor@correo.com"),
                new Contrasena("hash-simulado"),
                new Telefono("3001234567"),
                RolVendedor.CAFICULTOR);
    }

    @Test
    void dosVendedoresConLosMismosDatosNoSonElMismoPorqueElIdEsUnico() {
        Vendedor primero = vendedorValido("Carlos Perez");
        Vendedor segundo = vendedorValido("Carlos Perez");

        assertNotEquals(primero, segundo);
        assertNotEquals(primero.getId(), segundo.getId());
    }

    @Test
    void unVendedorEsIgualASiMismo() {
        Vendedor vendedor = vendedorValido("Carlos Perez");

        assertEquals(vendedor, vendedor);
    }

    @Test
    void noSePuedeRegistrarUnVendedorSinNombre() {
        assertThrows(ReglaDominioException.class, () -> Vendedor.registrar(
                "   ",
                new Email("caficultor@correo.com"),
                new Contrasena("hash-simulado"),
                new Telefono("3001234567"),
                RolVendedor.CAFICULTOR));
    }

    @Test
    void noSePuedeRegistrarUnVendedorSinEmail() {
        assertThrows(ReglaDominioException.class, () -> Vendedor.registrar(
                "Carlos Perez",
                null,
                new Contrasena("hash-simulado"),
                new Telefono("3001234567"),
                RolVendedor.CAFICULTOR));
    }

    @Test
    void noSePuedeRegistrarUnVendedorSinContrasena() {
        assertThrows(ReglaDominioException.class, () -> Vendedor.registrar(
                "Carlos Perez",
                new Email("caficultor@correo.com"),
                null,
                new Telefono("3001234567"),
                RolVendedor.CAFICULTOR));
    }

    @Test
    void noSePuedeRegistrarUnVendedorSinTelefono() {
        assertThrows(ReglaDominioException.class, () -> Vendedor.registrar(
                "Carlos Perez",
                new Email("caficultor@correo.com"),
                new Contrasena("hash-simulado"),
                null,
                RolVendedor.CAFICULTOR));
    }

    @Test
    void noSePuedeRegistrarUnVendedorSinRol() {
        assertThrows(ReglaDominioException.class, () -> Vendedor.registrar(
                "Carlos Perez",
                new Email("caficultor@correo.com"),
                new Contrasena("hash-simulado"),
                new Telefono("3001234567"),
                null));
    }

    @Test
    void unVendedorRecienRegistradoNoEstaEliminado() {
        Vendedor vendedor = vendedorValido("Carlos Perez");

        assertFalse(vendedor.isEliminadoLogicamente());
    }
}