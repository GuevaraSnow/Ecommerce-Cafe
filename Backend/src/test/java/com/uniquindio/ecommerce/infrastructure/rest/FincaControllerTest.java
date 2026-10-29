package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import com.uniquindio.ecommerce.application.usecase.RegistrarFinca;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.FincaMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FincaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FincaMapper.class)
class FincaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarFinca registrarFinca;

    @MockitoBean
    private FincaRepositorio fincaRepositorio;

    private Finca fincaValida() {
        return Finca.registrar(
                "finca-1",
                "caficultor-1",
                "Finca El Recuerdo",
                new Ubicacion("Quindío", "Armenia"),
                new Altitud(1550)
        );
    }

    @Test
    void registrarFincaResponde201() throws Exception {
        given(registrarFinca.ejecutar(any(), eq("caficultor-1"), eq("Finca El Recuerdo"), any(), any()))
                .willReturn(fincaValida());

        String bodyJson = """
                {
                    "nombre": "Finca El Recuerdo",
                    "ubicacion": { "departamento": "Quindío", "municipio": "Armenia" },
                    "altitud": 1550
                }
                """;

        mockMvc.perform(post("/api/fincas")
                        .header("X-Caficultor-Id", "caficultor-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value("finca-1"))
                .andExpect(jsonPath("$.nombre").value("Finca El Recuerdo"));
    }

    @Test
    void registrarFincaSinNombreResponde400() throws Exception {
        String bodyInvalido = """
                {
                    "nombre": "",
                    "ubicacion": { "departamento": "Quindío", "municipio": "Armenia" },
                    "altitud": 1550
                }
                """;

        mockMvc.perform(post("/api/fincas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarFincaPorIdResponde200() throws Exception {
        given(fincaRepositorio.buscarPorId("finca-1")).willReturn(Optional.of(fincaValida()));

        mockMvc.perform(get("/api/fincas/finca-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("finca-1"))
                .andExpect(jsonPath("$.nombre").value("Finca El Recuerdo"));
    }

    @Test
    void consultarFincaInexistenteResponde400o404() throws Exception {
        given(fincaRepositorio.buscarPorId("no-existe"))
                .willThrow(new ReglaDominioException("No se encontró la finca con id: no-existe"));

        mockMvc.perform(get("/api/fincas/no-existe"))
                .andExpect(status().isBadRequest());
    }
}