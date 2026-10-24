package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeBajaDePresentacion;
import com.uniquindio.ecommerce.Domain.service.ServicioDePublicacion;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReposicion;
import com.uniquindio.ecommerce.Domain.service.VerificadorDeEmail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra los Domain Services como beans de Spring. Los servicios del dominio
 * no llevan anotaciones (el dominio es Java puro): esta clase es la frontera
 * entre Spring y el dominio, y el único lugar donde se hace {@code new} de uno.
 * Cada servicio nuevo agrega aquí su método {@code @Bean}.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public ServicioDePublicacion servicioDePublicacion(LoteRepositorio loteRepositorio,
                                                       TransformacionRepositorio transformacionRepositorio, UsuarioRepositorio usuarioRepositorio) {
        return new ServicioDePublicacion(loteRepositorio, transformacionRepositorio, usuarioRepositorio);
    }

    @Bean
    public VerificadorDeEmail verificadorDeEmail(UsuarioRepositorio usuarioRepositorio) {
        return new VerificadorDeEmail(usuarioRepositorio);
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
