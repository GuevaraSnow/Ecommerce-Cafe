package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CompradorTest {

    @Test
    @DisplayName("Debería instanciar un Comprador correctamente cuando es mayor de edad (>= 18 años)")
    void crearComprador_deberiaExitoso_cuandoEsMayorDeEdad() {
        Email email = new Email("cliente@correo.com");
        Contrasena contrasena = new Contrasena("Clave1234");
        Telefono telefono = new Telefono("3001234567");
        FechaDeNacimiento fechaValida = new FechaDeNacimiento(LocalDate.now().minusYears(20));

        Comprador comprador = new Comprador(
                "COMP-001",
                "Juan Pérez",
                email,
                contrasena,
                telefono,
                fechaValida,
                TipoDeComprador.B2C
        );

        assertNotNull(comprador);
        assertEquals("COMP-001", comprador.getId());
        assertEquals(email, comprador.getEmail());
    }

    @Test
    @DisplayName("Debería lanzar ReglaDominioException al crear un Comprador si la FechaDeNacimiento corresponde a un menor de edad")
    void crearComprador_deberiaLanzarExcepcion_cuandoEsMenorDeEdad() {
        Email email = new Email("menor@correo.com");
        Contrasena contrasena = new Contrasena("Clave1234");
        Telefono telefono = new Telefono("3001234567");
        FechaDeNacimiento fechaMenorEdad = new FechaDeNacimiento(LocalDate.now().minusYears(15));

        // La regla de dominio se valida al instanciar Comprador
        assertThrows(ReglaDominioException.class, () -> {
            new Comprador(
                    "COMP-002",
                    "Pedro Menor",
                    email,
                    contrasena,
                    telefono,
                    fechaMenorEdad,
                    TipoDeComprador.B2C
            );
        });
    }
}