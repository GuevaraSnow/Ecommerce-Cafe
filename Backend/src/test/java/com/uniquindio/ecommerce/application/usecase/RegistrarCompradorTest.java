package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.CompradorRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarCompradorTest {

    private CompradorRepositorioMemoria compradorRepositorio;
    private RegistrarComprador registrarComprador;

    @BeforeEach
    void setUp() {
        compradorRepositorio = new CompradorRepositorioMemoria();
        registrarComprador = new RegistrarComprador(compradorRepositorio);
    }

    @Test
    @DisplayName("Debería registrar un comprador si el email no existe previamente")
    void ejecutar_deberiaRegistrarComprador_cuandoEmailEsUnico() {
        Email email = new Email("nuevo@correo.com");
        Contrasena contrasena = new Contrasena("Clave123");
        Telefono telefono = new Telefono("3000000000");
        FechaDeNacimiento fecha = new FechaDeNacimiento(LocalDate.now().minusYears(25));

        Comprador creado = registrarComprador.ejecutar(
                "COMP-100", "Carlos", email, contrasena, telefono, fecha, TipoDeComprador.B2C
        );

        assertNotNull(creado);
        assertTrue(compradorRepositorio.existePorEmail(email));
    }

    @Test
    @DisplayName("Debería lanzar ReglaDominioException si el email ya fue registrado")
    void ejecutar_deberiaLanzarExcepcion_cuandoEmailYaExiste() {
        Email email = new Email("existente@correo.com");
        Contrasena contrasena = new Contrasena("Clave123");
        Telefono telefono = new Telefono("3000000000");
        FechaDeNacimiento fecha = new FechaDeNacimiento(LocalDate.now().minusYears(25));

        registrarComprador.ejecutar("COMP-101", "Ana", email, contrasena, telefono, fecha, TipoDeComprador.B2C);

        assertThrows(ReglaDominioException.class, () ->
                registrarComprador.ejecutar("COMP-102", "Ana 2", email, contrasena, telefono, fecha, TipoDeComprador.B2C)
        );
    }
}