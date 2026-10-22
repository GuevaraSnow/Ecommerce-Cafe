package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReembolso;
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

    @Autowired
    private ServicioDeCompra servicioDeCompra;

    @Autowired
    private ServicioDeReembolso servicioDeReembolso;

    @Test
    void springRegistraLosDomainServicesDeCompraComoBeans() {
        assertNotNull(verificadorDeCompra);
        assertNotNull(servicioDeCompra);
        assertNotNull(servicioDeReembolso);
    }
}
