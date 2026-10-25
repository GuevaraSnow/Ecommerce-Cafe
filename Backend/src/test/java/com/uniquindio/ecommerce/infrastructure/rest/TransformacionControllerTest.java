package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.usecase.ConsultarFichaOrigen;
import com.uniquindio.ecommerce.application.usecase.RegistrarTransformacion;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.TransformacionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransformacionController.class)
@Import(TransformacionMapper.class)
class TransformacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarTransformacion registrarTransformacion;

    @MockitoBean
    private ConsultarFichaOrigen consultarFichaOrigen;

    private Transformacion transformacionValida() {
        return Transformacion.registrar(
                "trans-123",
                "LOT-2026-001",
                new Cantidad(50.0, "kg"),
                new PorcentajeDeMerma(0.18),
                "tostador-1",
                LocalDate.now(),
                PerfilTueste.MEDIO
        );
    }

    @Test
    void registrarTransformacionResponde201ConLocationYBody() throws Exception {
        given(registrarTransformacion.ejecutar(any(), eq("LOT-2026-001"), any(), any(), eq("tostador-1"), any(), eq(PerfilTueste.MEDIO)))
                .willReturn(transformacionValida());

        String jsonBody = """
                {
                    "loteCodigo": "LOT-2026-001",
                    "cantidadEntrada": { "valor": 50.0, "unidad": "kg" },
                    "merma": 0.18,
                    "tostadorId": "tostador-1",
                    "fecha": "2026-10-24",
                    "perfilTuesteResultante": "MEDIO"
                }
                """;

        mockMvc.perform(post("/api/transformaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value("trans-123"))
                .andExpect(jsonPath("$.loteCodigo").value("LOT-2026-001"))
                .andExpect(jsonPath("$.cantidadResultante.valor").value(41.0));
    }

    @Test
    void registrarTransformacionInvalidaResponde400() throws Exception {
        String jsonInvalido = """
                {
                    "loteCodigo": "",
                    "merma": 0.18
                }
                """;

        mockMvc.perform(post("/api/transformaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarFichaOrigenResponde200() throws Exception {
        FichaDeOrigen ficha = new FichaDeOrigen(
                "Finca La Aurora",
                "Año: 2026 - Temporada: PRINCIPAL",
                Collections.emptyList(),
                "trans-123",
                "tostador-1",
                LocalDate.now(),
                PerfilTueste.MEDIO
        );

        given(consultarFichaOrigen.ejecutar("trans-123")).willReturn(ficha);

        mockMvc.perform(get("/api/transformaciones/trans-123/ficha-origen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fincaNombre").value("Finca La Aurora"))
                .andExpect(jsonPath("$.transformacionId").value("trans-123"));
    }

    @Test
    void consultarFichaOrigenInexistenteResponde404() throws Exception {
        given(consultarFichaOrigen.ejecutar("trans-inexistente"))
                .willThrow(new ReglaDominioException("No se encontró la transformación especificada: trans-inexistente"));

        mockMvc.perform(get("/api/transformaciones/trans-inexistente/ficha-origen"))
                .andExpect(status().isBadRequest());
    }
}