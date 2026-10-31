package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.usecase.ConsultarFichaOrigen;
import com.uniquindio.ecommerce.application.usecase.RegistrarTransformacion;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.TransformacionMapper;
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

import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TransformacionController: códigos HTTP (201, 200, 400, 401, 403, 404) y que el Tostador sale del token, no del
 * cuerpo. Los casos de uso están simulados; la seguridad y el mapper son los reales.
 */
@WebMvcTest(TransformacionController.class)
@Import({TransformacionMapper.class, SecurityConfig.class})
class TransformacionControllerTest {

    private static final String TRANSFORMACION = """
            {
                "loteCodigo": "LOT-2026-001",
                "cantidadEntrada": { "valor": 50.0, "unidad": "kg" },
                "merma": 0.18,
                "fecha": "2026-10-24",
                "perfilTuesteResultante": "MEDIO"
            }""";

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarTransformacion registrarTransformacion;

    @MockitoBean
    private ConsultarFichaOrigen consultarFichaOrigen;

    @BeforeEach
    void prepararLaPeticion() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
    }

    private RequestPostProcessor token(String usuarioId, String rol) {
        return jwt().jwt(token -> token.subject(usuarioId).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

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

    // ----- registrar -----

    @Test
    void registrarResponde201ConLocationYElTostadorDelToken() throws Exception {
        given(registrarTransformacion.ejecutar(any(), eq(new CodigoDeLote("LOT-2026-001")), any(), any(),
                eq("tostador-1"), any(), eq(PerfilTueste.MEDIO))).willReturn(transformacionValida());

        mockMvc.perform(post("/api/transformaciones").with(token("tostador-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value("trans-123"))
                .andExpect(jsonPath("$.loteCodigo").value("LOT-2026-001"))
                .andExpect(jsonPath("$.tostadorId").value("tostador-1"))
                .andExpect(jsonPath("$.cantidadResultante.valor").value(41.0));
    }

    @Test
    void registrarIgnoraUnTostadorIdQueLlegueEnElCuerpo() throws Exception {
        given(registrarTransformacion.ejecutar(any(), any(), any(), any(), eq("tostador-1"), any(), any()))
                .willReturn(transformacionValida());

        mockMvc.perform(post("/api/transformaciones").with(token("tostador-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loteCodigo": "LOT-2026-001", "cantidadEntrada": {"valor": 50.0, "unidad": "kg"},
                                 "merma": 0.18, "tostadorId": "otro-tostador", "fecha": "2026-10-24",
                                 "perfilTuesteResultante": "MEDIO"}"""))
                .andExpect(status().isCreated());

        verify(registrarTransformacion).ejecutar(any(), any(), any(), any(), eq("tostador-1"), any(), any());
    }

    @Test
    void registrarSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/transformaciones").contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isUnauthorized());

        verify(registrarTransformacion, never()).ejecutar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void registrarConUnRolQueNoEsTostadorResponde403() throws Exception {
        mockMvc.perform(post("/api/transformaciones").with(token("comprador-1", "COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/transformaciones").with(token("caficultor-1", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isForbidden());

        verify(registrarTransformacion, never()).ejecutar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void registrarConDatosInvalidosResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/transformaciones").with(token("tostador-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loteCodigo": "", "merma": 0.18}"""))
                .andExpect(status().isBadRequest());

        verify(registrarTransformacion, never()).ejecutar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void registrarConUnaMermaFueraDeRangoResponde422PorElDominio() throws Exception {
        mockMvc.perform(post("/api/transformaciones").with(token("tostador-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRANSFORMACION.replace("0.18", "0.50")))
                .andExpect(status().isUnprocessableEntity());

        verify(registrarTransformacion, never()).ejecutar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void registrarSobreUnLoteQueNoExisteResponde404() throws Exception {
        given(registrarTransformacion.ejecutar(any(), any(), any(), any(), any(), any(), any()))
                .willThrow(new NoSuchElementException());

        mockMvc.perform(post("/api/transformaciones").with(token("tostador-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ----- ficha de origen -----

    @Test
    void consultarFichaOrigenResponde200ConCualquierRolAutenticado() throws Exception {
        FichaDeOrigen ficha = new FichaDeOrigen(
                "Finca La Aurora",
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                Collections.emptyList(),
                "trans-123",
                "tostador-1",
                LocalDate.now(),
                PerfilTueste.MEDIO
        );
        given(consultarFichaOrigen.ejecutar("trans-123")).willReturn(ficha);

        mockMvc.perform(get("/api/transformaciones/trans-123/ficha-origen").with(token("comprador-1", "COMPRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fincaNombre").value("Finca La Aurora"))
                .andExpect(jsonPath("$.cosechaInfo").value("Año: " + Year.now().getValue() + " - Temporada: PRINCIPAL"))
                .andExpect(jsonPath("$.transformacionId").value("trans-123"));
    }

    @Test
    void consultarFichaOrigenSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/transformaciones/trans-123/ficha-origen")).andExpect(status().isUnauthorized());
    }

    @Test
    void consultarFichaOrigenInexistenteResponde404() throws Exception {
        given(consultarFichaOrigen.ejecutar("trans-inexistente")).willThrow(new NoSuchElementException());

        mockMvc.perform(get("/api/transformaciones/trans-inexistente/ficha-origen").with(token("comprador-1", "COMPRADOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"));
    }
}
