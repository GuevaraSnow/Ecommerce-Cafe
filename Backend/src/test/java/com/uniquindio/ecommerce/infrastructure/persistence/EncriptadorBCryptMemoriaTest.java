package com.uniquindio.ecommerce.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncriptadorBCryptMemoriaTest {

    private final EncriptadorBCryptMemoria encriptador = new EncriptadorBCryptMemoria();

    @Test
    void mismaContrasenaValidaCorrectamente() {
        String contrasenaPlana = "Password123!";
        String hash = encriptador.encriptar(contrasenaPlana);

        assertTrue(encriptador.coinciden(contrasenaPlana, hash));
    }

    @Test
    void contrasenaDistintaEsRechazada() {
        String contrasenaPlana = "Password123!";
        String hash = encriptador.encriptar(contrasenaPlana);

        assertFalse(encriptador.coinciden("OtraContrasena456!", hash));
    }
}