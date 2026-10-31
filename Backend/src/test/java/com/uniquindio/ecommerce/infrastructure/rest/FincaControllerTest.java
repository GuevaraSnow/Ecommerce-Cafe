package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import com.uniquindio.ecommerce.application.usecase.ConsultarFinca;
import com.uniquindio.ecommerce.application.usecase.RegistrarFinca;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.FincaMapper;
import com.uniquindio.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FincaController: códigos HTTP (201, 200, 400, 401, 403, 404) y que el propietario sale del token, no de un
 * encabezado. Los casos de uso están simulados; la seguridad es la real.
 */
@WebMvcTest(FincaController.class)
@Import({FincaMapper.class, SecurityConfig.class})
class FincaControllerTest {

    private static final String FINCA = """
            {"nombre": "Finca El Recuerdo", "ubicacion": {"departamento": "Quindío", "municipio": "Armenia"}, "altitud": 1550}""";

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarFinca registrarFinca;

    @MockitoBean
    private ConsultarFinca consultarFinca;

    @BeforeEach
    void prepararLaPeticion() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private RequestPostProcessor token(String usuarioId, String rol) {
        return jwt().jwt(token -> token.subject(usuarioId).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    private Finca finca() {
        return Finca.registrar("finca-1", "caficultor-1", "Finca El Recuerdo",
                new Ubicacion("Quindío", "Armenia"), new Altitud(1550));
    }

    // ----- registrar -----

    @Test
    void registrarResponde201ConLocationYElCaficultorDelToken() throws Exception {
        given(registrarFinca.ejecutar(any(), eq("caficultor-1"), eq("Finca El Recuerdo"), any(), any()))
                .willReturn(finca());

        mockMvc.perform(post("/api/fincas").with(token("caficultor-1", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/fincas/finca-1"))
                .andExpect(jsonPath("$.id").value("finca-1"))
                .andExpect(jsonPath("$.propietarioId").value("caficultor-1"));
    }

    @Test
    void registrarIgnoraUnEncabezadoConOtroPropietario() throws Exception {
        given(registrarFinca.ejecutar(any(), eq("caficultor-1"), any(), any(), any())).willReturn(finca());

        mockMvc.perform(post("/api/fincas").with(token("caficultor-1", "CAFICULTOR"))
                        .header("X-Caficultor-Id", "otro-caficultor")
                        .contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isCreated());

        verify(registrarFinca).ejecutar(any(), eq("caficultor-1"), any(), any(), any());
    }

    @Test
    void registrarSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/fincas").contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isUnauthorized());

        verify(registrarFinca, never()).ejecutar(any(), any(), any(), any(), any());
    }

    @Test
    void registrarConUnRolQueNoEsCaficultorResponde403() throws Exception {
        mockMvc.perform(post("/api/fincas").with(token("comprador-1", "COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/fincas").with(token("tostador-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isForbidden());

        verify(registrarFinca, never()).ejecutar(any(), any(), any(), any(), any());
    }

    @Test
    void registrarSinNombreResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/fincas").with(token("caficultor-1", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "ubicacion": {"departamento": "Quindío", "municipio": "Armenia"}, "altitud": 1550}"""))
                .andExpect(status().isBadRequest());

        verify(registrarFinca, never()).ejecutar(any(), any(), any(), any(), any());
    }

    // ----- consultar -----

    @Test
    void consultarResponde200ConCualquierRolAutenticado() throws Exception {
        given(consultarFinca.ejecutar("finca-1")).willReturn(finca());

        mockMvc.perform(get("/api/fincas/finca-1").with(token("comprador-1", "COMPRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("finca-1"))
                .andExpect(jsonPath("$.nombre").value("Finca El Recuerdo"));
    }

    @Test
    void consultarSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/fincas/finca-1")).andExpect(status().isUnauthorized());
    }

    @Test
    void consultarUnaFincaInexistenteResponde404() throws Exception {
        given(consultarFinca.ejecutar("no-existe")).willThrow(new NoSuchElementException());

        mockMvc.perform(get("/api/fincas/no-existe").with(token("comprador-1", "COMPRADOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"));
    }
}
