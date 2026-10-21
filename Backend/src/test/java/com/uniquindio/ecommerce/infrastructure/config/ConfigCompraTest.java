package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.service.VerificadorDeCompra;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Comprueba que Spring arma los Domain Services del vertical de Compra con sus repositorios. */
@SpringBootTest
class ConfigCompraTest {

    @Autowired
    private VerificadorDeCompra verificadorDeCompra;

    @Test
    void springRegistraLosDomainServicesDeCompraComoBeans() {
        assertNotNull(verificadorDeCompra);
    }
}
