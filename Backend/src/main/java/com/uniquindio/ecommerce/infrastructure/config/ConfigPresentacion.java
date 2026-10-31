package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeBajaDePresentacion;
import com.uniquindio.ecommerce.Domain.service.ServicioDePublicacion;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReposicion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra como beans los Domain Services del vertical de Presentación: publicar, reponer stock y dar
 * de baja. Es el único lugar donde se hace {@code new} de ellos (el dominio no lleva anotaciones de Spring).
 */
@Configuration
public class ConfigPresentacion {

    @Bean
    public ServicioDePublicacion servicioDePublicacion(LoteRepositorio loteRepositorio,
                                                       TransformacionRepositorio transformacionRepositorio,
                                                       UsuarioRepositorio usuarioRepositorio) {
        return new ServicioDePublicacion(loteRepositorio, transformacionRepositorio, usuarioRepositorio);
    }

    @Bean
    public ServicioDeReposicion servicioDeReposicion(LoteRepositorio loteRepositorio,
                                                     TransformacionRepositorio transformacionRepositorio) {
        return new ServicioDeReposicion(loteRepositorio, transformacionRepositorio);
    }

    @Bean
    public ServicioDeBajaDePresentacion servicioDeBajaDePresentacion(CompraRepositorio compraRepositorio) {
        return new ServicioDeBajaDePresentacion(compraRepositorio);
    }
}
