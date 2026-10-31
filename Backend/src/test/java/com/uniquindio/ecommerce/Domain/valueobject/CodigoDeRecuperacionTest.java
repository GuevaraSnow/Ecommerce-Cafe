package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CodigoDeRecuperacionTest {

    @Test
    void generarCodigoCreaCodigo6DigitosYVigente() {
        CodigoDeRecuperacion codigo = CodigoDeRecuperacion.generar();

        assertNotNull(codigo.codigo());
        assertEquals(6, codigo.codigo().length());
        assertTrue(codigo.estaVigente());
    }

    @Test
    void codigoExpiradoRetornaEstaVigenteFalse() {
        LocalDateTime fechaExpirada = LocalDateTime.now().minusMinutes(16);
        CodigoDeRecuperacion codigoExpirado = new CodigoDeRecuperacion("123456", fechaExpirada);

        assertFalse(codigoExpirado.estaVigente());
    }

    @Test
    void coincideConValidaCorrectamenteElCodigoEnTexto() {
        CodigoDeRecuperacion codigo = new CodigoDeRecuperacion("654321", LocalDateTime.now().plusMinutes(15));

        assertTrue(codigo.coincideCon("654321"));
        assertTrue(codigo.coincideCon(" 654321 "));
        assertFalse(codigo.coincideCon("111111"));
        assertFalse(codigo.coincideCon(null));
    }

    @Test
    void rechazaCodigoNuloOVacio() {
        assertThrows(ReglaDominioException.class, () -> new CodigoDeRecuperacion(null, LocalDateTime.now()));
        assertThrows(ReglaDominioException.class, () -> new CodigoDeRecuperacion("   ", LocalDateTime.now()));
    }

    @Test
    void rechazaFechaExpiracionNula() {
        assertThrows(ReglaDominioException.class, () -> new CodigoDeRecuperacion("123456", null));
    }
}