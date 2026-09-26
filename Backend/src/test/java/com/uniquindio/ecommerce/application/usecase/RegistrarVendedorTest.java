package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Vendedor;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.RolVendedor;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import com.uniquindio.ecommerce.infrastructure.persistence.VendedorRepositorioMemoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarVendedorTest {

    @Test
    void registrarUnVendedorNuevoLoDejaDisponibleEnElRepositorio() {
        VendedorRepositorioMemoria repositorio = new VendedorRepositorioMemoria();
        RegistrarVendedor registrarVendedor = new RegistrarVendedor(repositorio);

        Vendedor vendedor = registrarVendedor.ejecutar(
                "Carlos Perez",
                new Email("caficultor@correo.com"),
                new Contrasena("hash-simulado"),
                new Telefono("3001234567"),
                RolVendedor.CAFICULTOR);

        assertTrue(repositorio.existePorEmail("caficultor@correo.com"));
        assertEquals("Carlos Perez", vendedor.getNombre());
    }

    @Test
    void noSePuedeRegistrarDosVendedoresConElMismoEmail() {
        VendedorRepositorioMemoria repositorio = new VendedorRepositorioMemoria();
        RegistrarVendedor registrarVendedor = new RegistrarVendedor(repositorio);

        registrarVendedor.ejecutar(
                "Carlos Perez",
                new Email("caficultor@correo.com"),
                new Contrasena("hash-simulado"),
                new Telefono("3001234567"),
                RolVendedor.CAFICULTOR);

        assertThrows(ReglaDominioException.class, () -> registrarVendedor.ejecutar(
                "Otro Vendedor",
                new Email("caficultor@correo.com"),
                new Contrasena("otro-hash"),
                new Telefono("3009876543"),
                RolVendedor.TOSTADOR));
    }
}