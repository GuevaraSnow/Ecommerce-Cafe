package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarFincaTest {

    @Test
    void ejecutarRegistraUnaFincaCorrectamente() {
        RegistrarFinca registrarFinca = new RegistrarFinca();

        Finca finca = registrarFinca.ejecutar(
                "finca-1",
                "caficultor-1",
                "Finca La Esperanza",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650));

        assertEquals("Finca La Esperanza", finca.getNombre());
        assertFalse(finca.isEliminada());
    }

    @Test
    void ejecutarPropagaLaValidacionDelDominio() {
        RegistrarFinca registrarFinca = new RegistrarFinca();

        assertThrows(ReglaDominioException.class, () -> registrarFinca.ejecutar(
                "finca-1",
                "caficultor-1",
                "",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650)));
    }
}