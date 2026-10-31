package com.uniquindio.ecommerce.infrastructure.config;

import com.uniquindio.ecommerce.Domain.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cada puerto de repositorio tiene un solo adaptador activo, el JPA, sin necesidad de {@code @Primary}: los
 * repositorios en memoria ya no son beans (solo sirven para las pruebas unitarias) y se crean a mano.
 */
@SpringBootTest
class UnSoloAdaptadorPorPuertoTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    void cadaPuertoDeRepositorioTieneUnSoloBeanYEsElAdaptadorJpa() {
        List<Class<?>> puertos = List.of(
                UsuarioRepositorio.class, FincaRepositorio.class, LoteRepositorio.class,
                TransformacionRepositorio.class, PresentacionRepositorio.class, CompraRepositorio.class,
                ResenaRepositorio.class, SolicitudReembolsoRepositorio.class);

        for (Class<?> puerto : puertos) {
            var beans = contexto.getBeansOfType(puerto);

            assertEquals(1, beans.size(), "Debe haber un solo bean de " + puerto.getSimpleName() + " y hay " + beans.keySet());
            Class<?> adaptador = beans.values().iterator().next().getClass();
            assertTrue(adaptador.getSimpleName().contains("RepositorioJpa"),
                    puerto.getSimpleName() + " lo implementa " + adaptador.getSimpleName() + ", no un adaptador JPA");
        }
    }
}
