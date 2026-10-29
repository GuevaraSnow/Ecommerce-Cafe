package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import com.uniquindio.ecommerce.application.usecase.ConsultarResenasDePresentacion;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.ResenaMapper;
import com.uniquindio.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** ResenaConsultaController: la lista de reseñas de una Presentación es pública, así que se prueba sin token. */
@WebMvcTest(ResenaConsultaController.class)
@Import({ResenaMapper.class, SecurityConfig.class})
class ResenaConsultaControllerTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarResenasDePresentacion consultarResenas;

    @BeforeEach
    void prepararLaPeticion() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private Resena resena(String id, int calificacion) {
        return Resena.dejar(id, "comprador-" + id, "pres-1", "vendedor-1", new Calificacion(calificacion),
                new TextoDeResena("Comentario " + id), LocalDateTime.of(2026, 10, 20, 18, 0));
    }

    @Test
    void cualquieraVeLasResenasDeUnaPresentacionSinToken() throws Exception {
        given(consultarResenas.ejecutar("pres-1")).willReturn(List.of(resena("resena-2", 5), resena("resena-1", 3)));

        mockMvc.perform(get("/api/resenas").param("presentacionId", "pres-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("resena-2"))
                .andExpect(jsonPath("$[0].calificacion").value(5))
                .andExpect(jsonPath("$[1].id").value("resena-1"));
    }

    @Test
    void unaPresentacionSinResenasResponde200ConUnaListaVacia() throws Exception {
        given(consultarResenas.ejecutar("pres-sin-resenas")).willReturn(List.of());

        mockMvc.perform(get("/api/resenas").param("presentacionId", "pres-sin-resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void sinIndicarLaPresentacionResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(get("/api/resenas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro 'presentacionId'"));

        verify(consultarResenas, never()).ejecutar(any());
    }
}
