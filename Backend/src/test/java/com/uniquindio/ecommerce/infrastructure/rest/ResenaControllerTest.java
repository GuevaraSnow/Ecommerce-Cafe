package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import com.uniquindio.ecommerce.application.usecase.DejarResena;
import com.uniquindio.ecommerce.application.usecase.ResponderResena;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.ResenaMapper;
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

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ResenaController: dejar y responder reseñas. Códigos HTTP y que quien actúa sale del token; las reglas de
 * compra entregada, una sola reseña y vendedor dueño las valida el dominio y se prueban allá.
 */
@WebMvcTest(ResenaController.class)
@Import({ResenaMapper.class, SecurityConfig.class})
class ResenaControllerTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @MockitoBean
    private DejarResena dejarResena;
    @MockitoBean
    private ResponderResena responderResena;

    private static final String RESENA = """
            {"presentacionId": "pres-1", "calificacion": 5, "texto": "Notas a panela"}""";
    private static final String RESPUESTA = """
            {"texto": "Gracias por tu compra"}""";
    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 20, 18, 0);

    /** Por defecto las peticiones llevan el token del Comprador; un test puede poner otro con .with(token(...)). */
    @BeforeEach
    void prepararLaPeticion() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .defaultRequest(get("/").with(token("comprador-1", "COMPRADOR")))
                .build();
    }

    private RequestPostProcessor token(String usuarioId, String rol) {
        return jwt().jwt(token -> token.subject(usuarioId).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    private Resena resena() {
        return Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1", new Calificacion(5),
                new TextoDeResena("Notas a panela"), FECHA);
    }

    // ----- dejar -----

    @Test
    void dejarResponde201ConLocationYElCompradorDelToken() throws Exception {
        given(dejarResena.ejecutar(anyString(), eq("comprador-1"), eq("pres-1"), eq(new Calificacion(5)),
                eq(new TextoDeResena("Notas a panela")))).willReturn(resena());

        mockMvc.perform(post("/api/resenas").contentType(MediaType.APPLICATION_JSON).content(RESENA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/resenas/resena-1"))
                .andExpect(jsonPath("$.id").value("resena-1"))
                .andExpect(jsonPath("$.calificacion").value(5))
                .andExpect(jsonPath("$.respuesta").doesNotExist());
    }

    @Test
    void dejarConCalificacionFueraDeRangoResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/resenas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"presentacionId": "pres-1", "calificacion": 6, "texto": "Excelente"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("La calificacion maxima es 5")));

        verify(dejarResena, never()).ejecutar(any(), any(), any(), any(), any());
    }

    @Test
    void dejarSinHaberRecibidoLaPresentacionResponde422ConElMensajeDelDominio() throws Exception {
        given(dejarResena.ejecutar(any(), any(), any(), any(), any())).willThrow(
                new ReglaDominioException("Solo puede resenar quien recibio la Presentacion"));

        mockMvc.perform(post("/api/resenas").contentType(MediaType.APPLICATION_JSON).content(RESENA))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Solo puede resenar quien recibio la Presentacion"));
    }

    @Test
    void dejarSobreUnaPresentacionQueNoExisteResponde404() throws Exception {
        given(dejarResena.ejecutar(any(), any(), any(), any(), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(post("/api/resenas").contentType(MediaType.APPLICATION_JSON).content(RESENA))
                .andExpect(status().isNotFound());
    }

    @Test
    void unVendedorNoPuedeDejarResenaYResponde403() throws Exception {
        mockMvc.perform(post("/api/resenas").with(token("vendedor-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(RESENA))
                .andExpect(status().isForbidden());

        verify(dejarResena, never()).ejecutar(any(), any(), any(), any(), any());
    }

    // ----- responder -----

    @Test
    void elVendedorRespondeYResponde200ConLaRespuesta() throws Exception {
        Resena respondida = resena();
        respondida.responder("vendedor-1", new TextoDeResena("Gracias por tu compra"), FECHA.plusHours(1));
        given(responderResena.ejecutar("resena-1", "vendedor-1", new TextoDeResena("Gracias por tu compra")))
                .willReturn(respondida);

        mockMvc.perform(put("/api/resenas/resena-1/respuesta").with(token("vendedor-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(RESPUESTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta.texto").value("Gracias por tu compra"));
    }

    @Test
    void responderSinTextoResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(put("/api/resenas/resena-1/respuesta").with(token("vendedor-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"texto": " "}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("La respuesta es obligatoria")));

        verify(responderResena, never()).ejecutar(any(), any(), any());
    }

    @Test
    void responderUnaResenaYaRespondidaResponde422() throws Exception {
        given(responderResena.ejecutar(any(), any(), any()))
                .willThrow(new ReglaDominioException("La resena ya fue respondida"));

        mockMvc.perform(put("/api/resenas/resena-1/respuesta").with(token("vendedor-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(RESPUESTA))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("La resena ya fue respondida"));
    }

    @Test
    void responderUnaResenaQueNoExisteResponde404() throws Exception {
        given(responderResena.ejecutar(eq("no-existe"), any(), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/resenas/no-existe/respuesta").with(token("vendedor-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(RESPUESTA))
                .andExpect(status().isNotFound());
    }

    @Test
    void elCompradorNoPuedeResponderYResponde403() throws Exception {
        mockMvc.perform(put("/api/resenas/resena-1/respuesta")
                        .contentType(MediaType.APPLICATION_JSON).content(RESPUESTA))
                .andExpect(status().isForbidden());

        verify(responderResena, never()).ejecutar(any(), any(), any());
    }
}
