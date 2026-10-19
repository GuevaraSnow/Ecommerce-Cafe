package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.service.ServicioDeBajaDePresentacion;
import com.uniquindio.ecommerce.Domain.service.ServicioDePublicacion;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReposicion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Comprueba que Spring arma los Domain Services con los repositorios que necesitan. */
@SpringBootTest
class DomainServiceConfigTest {

    @Autowired
    private ServicioDePublicacion servicioDePublicacion;

    @Autowired
    private ServicioDeReposicion servicioDeReposicion;

    @Autowired
    private ServicioDeBajaDePresentacion servicioDeBajaDePresentacion;

    @Test
    void springRegistraLosDomainServicesComoBeans() {
        assertNotNull(servicioDePublicacion);
        assertNotNull(servicioDeReposicion);
        assertNotNull(servicioDeBajaDePresentacion);
    }
}
