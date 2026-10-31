package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.repository.*;
import com.uniquindio.ecommerce.Domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra como beans los Domain Services que no pertenecen a un vertical con su propia clase de
 * configuración (Usuario y Lote). Los servicios del dominio no llevan anotaciones (el dominio es Java
 * puro): las clases {@code Config*} son la frontera entre Spring y el dominio, y el único lugar donde se
 * hace {@code new} de un servicio. Presentación, Compra y Transformación tienen la suya
 * ({@link ConfigPresentacion}, {@link ConfigCompra}, {@link ConfigTransformacion}).
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public VerificadorDeEmail verificadorDeEmail(UsuarioRepositorio usuarioRepositorio) {
        return new VerificadorDeEmail(usuarioRepositorio);
    }

    @Bean
    public ServicioDeAutenticacion servicioDeAutenticacion(UsuarioRepositorio usuarioRepositorio,
                                                           EncriptadorContrasenaRepositorio encriptador) {
        return new ServicioDeAutenticacion(usuarioRepositorio, encriptador);
    }

    @Bean
    public ServicioDeContrasena servicioDeContrasena(EncriptadorContrasenaRepositorio encriptador) {
        return new ServicioDeContrasena(encriptador);
    }

    @Bean
    public ServicioDeLote servicioDeLote(LoteRepositorio loteRepositorio) {
        return new ServicioDeLote(loteRepositorio);
    }
}
