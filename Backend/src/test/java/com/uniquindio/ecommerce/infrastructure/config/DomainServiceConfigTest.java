package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.service.ServicioDeContrasena;
import com.uniquindio.ecommerce.Domain.service.ServicioDeLote;
import com.uniquindio.ecommerce.Domain.service.VerificadorDeEmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Comprueba que Spring arma los Domain Services de Usuario y Lote con los repositorios que necesitan. */
@SpringBootTest
class DomainServiceConfigTest {

    @Autowired
    private VerificadorDeEmail verificadorDeEmail;

    @Autowired
    private ServicioDeContrasena servicioDeContrasena;

    @Autowired
    private ServicioDeLote servicioDeLote;

    @Test
    void springRegistraLosDomainServicesComoBeans() {
        assertNotNull(verificadorDeEmail);
        assertNotNull(servicioDeContrasena);
        assertNotNull(servicioDeLote);
    }
}
