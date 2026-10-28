package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReembolso;
import com.uniquindio.ecommerce.Domain.service.VerificadorDeCompra;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra como beans los Domain Services del vertical de Compra, Reembolso y Reseña. Es el
 * único lugar donde se hace {@code new} de ellos (el dominio no lleva anotaciones de Spring).
 */
@Configuration
public class ConfigCompra {

    @Bean
    public VerificadorDeCompra verificadorDeCompra(CompraRepositorio compraRepositorio,
                                                   ResenaRepositorio resenaRepositorio) {
        return new VerificadorDeCompra(compraRepositorio, resenaRepositorio);
    }

    @Bean
    public ServicioDeCompra servicioDeCompra(PresentacionRepositorio presentacionRepositorio) {
        return new ServicioDeCompra(presentacionRepositorio);
    }

    @Bean
    public ServicioDeReembolso servicioDeReembolso(SolicitudReembolsoRepositorio solicitudReembolsoRepositorio) {
        return new ServicioDeReembolso(solicitudReembolsoRepositorio);
    }
}
