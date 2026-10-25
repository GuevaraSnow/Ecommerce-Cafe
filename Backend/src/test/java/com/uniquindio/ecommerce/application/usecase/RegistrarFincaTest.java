package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import com.uniquindio.ecommerce.infrastructure.persistence.FincaRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarFincaTest {

    private FincaRepositorio fincaRepositorio;
    private RegistrarFinca registrarFinca;

    @BeforeEach
    void setUp() {
        fincaRepositorio = new FincaRepositorioMemoria();
        registrarFinca = new RegistrarFinca(fincaRepositorio);
    }

    @Test
    void ejecutarRegistraUnaFincaCorrectamente() {
        Finca finca = registrarFinca.ejecutar(
                "finca-1",
                "caficultor-1",
                "Finca La Esperanza",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650));

        assertEquals("Finca La Esperanza", finca.getNombre());
        assertFalse(finca.isEliminada());
        assertTrue(fincaRepositorio.buscarPorId("finca-1").isPresent());
    }

    @Test
    void ejecutarPropagaLaValidacionDelDominio() {
        assertThrows(ReglaDominioException.class, () -> registrarFinca.ejecutar(
                "finca-1",
                "caficultor-1",
                "",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650)));
    }
}