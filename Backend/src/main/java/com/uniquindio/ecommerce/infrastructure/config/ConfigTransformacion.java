package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeTransformacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigTransformacion {

    @Bean
    public ServicioDeTransformacion servicioDeTransformacion(
            TransformacionRepositorio transformacionRepositorio,
            LoteRepositorio loteRepositorio
    ) {
        return new ServicioDeTransformacion(transformacionRepositorio, loteRepositorio);
    }
}