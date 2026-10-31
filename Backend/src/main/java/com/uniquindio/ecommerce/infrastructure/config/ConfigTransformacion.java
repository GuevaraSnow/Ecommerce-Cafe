package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.service.ServicioDeTransformacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra como bean el Domain Service del vertical de Transformación (el dominio no lleva anotaciones de Spring). */
@Configuration
public class ConfigTransformacion {

    @Bean
    public ServicioDeTransformacion servicioDeTransformacion() {
        return new ServicioDeTransformacion();
    }
}
