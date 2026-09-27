package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FincaTest {

    private Finca fincaValida(String id, String propietarioId) {
        return Finca.registrar(
                id,
                propietarioId,
                "Finca La Esperanza",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650));
    }

    @Test
    void dosFincasConElMismoIdSonLaMismaAunqueCambienOtrosDatos() {
        Finca original = fincaValida("finca-1", "caficultor-1");
        Finca otra = fincaValida("finca-1", "caficultor-2");

        assertEquals(original, otra);
    }

    @Test
    void noSePuedeRegistrarUnaFincaSinIdentificador() {
        assertThrows(ReglaDominioException.class, () -> Finca.registrar(
                "",
                "caficultor-1",
                "Finca La Esperanza",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650)));
    }

    @Test
    void noSePuedeRegistrarUnaFincaSinPropietario() {
        assertThrows(ReglaDominioException.class, () -> Finca.registrar(
                "finca-1",
                null,
                "Finca La Esperanza",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650)));
    }

    @Test
    void noSePuedeRegistrarUnaFincaSinUbicacion() {
        assertThrows(ReglaDominioException.class, () -> Finca.registrar(
                "finca-1",
                "caficultor-1",
                "Finca La Esperanza",
                null,
                new Altitud(1650)));
    }

    @Test
    void noSePuedeRegistrarUnaFincaSinAltitud() {
        assertThrows(ReglaDominioException.class, () -> Finca.registrar(
                "finca-1",
                "caficultor-1",
                "Finca La Esperanza",
                new Ubicacion("Quindio", "Montenegro"),
                null));
    }

    @Test
    void noSePuedeRegistrarUnaFincaSinNombre() {
        assertThrows(ReglaDominioException.class, () -> Finca.registrar(
                "finca-1",
                "caficultor-1",
                "   ",
                new Ubicacion("Quindio", "Montenegro"),
                new Altitud(1650)));
    }

    @Test
    void unaFincaRecienRegistradaNoEstaEliminada() {
        Finca finca = fincaValida("finca-1", "caficultor-1");

        assertFalse(finca.isEliminada());
    }

    @Test
    void eliminarMarcaLaFincaComoEliminada() {
        Finca finca = fincaValida("finca-1", "caficultor-1");

        finca.eliminar();

        assertTrue(finca.isEliminada());
    }
}