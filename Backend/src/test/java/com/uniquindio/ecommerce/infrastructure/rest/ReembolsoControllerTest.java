package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.application.usecase.AprobarReembolso;
import com.uniquindio.ecommerce.application.usecase.RechazarReembolso;
import com.uniquindio.ecommerce.application.usecase.SolicitarReembolso;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.ReembolsoMapper;
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
 * ReembolsoController: códigos HTTP (201, 200, 400, 401, 403, 404) y que quien actúa sale del token.
 * Las reglas de 48 h, dueño de la Compra y vendedor dueño las valida el dominio y se prueban allá.
 */
@WebMvcTest(ReembolsoController.class)
@Import({ReembolsoMapper.class, SecurityConfig.class})
class ReembolsoControllerTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @MockitoBean
    private SolicitarReembolso solicitarReembolso;
    @MockitoBean
    private AprobarReembolso aprobarReembolso;
    @MockitoBean
    private RechazarReembolso rechazarReembolso;

    private static final String SOLICITUD = """
            {"compraId": "compra-1", "presentacionId": "pres-1", "motivo": "Llego humedo"}""";
    private static final LocalDateTime SOLICITADA = LocalDateTime.of(2026, 10, 21, 10, 0);

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

    private SolicitudReembolso solicitud() {
        return SolicitudReembolso.solicitar("reembolso-1", "compra-1", "pres-1", "vendedor-1", "Llego humedo", SOLICITADA);
    }

    // ----- solicitar -----

    @Test
    void solicitarResponde201ConLocationYElCompradorDelToken() throws Exception {
        given(solicitarReembolso.ejecutar(anyString(), eq("compra-1"), eq("comprador-1"), eq("pres-1"), eq("Llego humedo")))
                .willReturn(solicitud());

        mockMvc.perform(post("/api/reembolsos").contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/reembolsos/reembolso-1"))
                .andExpect(jsonPath("$.id").value("reembolso-1"))
                .andExpect(jsonPath("$.estado").value("SOLICITADO"))
                .andExpect(jsonPath("$.vendedorId").value("vendedor-1"));
    }

    @Test
    void solicitarSinDatosResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/reembolsos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compraId": "", "presentacionId": "pres-1", "motivo": ""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("La Compra a reembolsar es obligatoria")))
                .andExpect(jsonPath("$.mensaje").value(containsString("El motivo es obligatorio")));

        verify(solicitarReembolso, never()).ejecutar(any(), any(), any(), any(), any());
    }

    @Test
    void solicitarFueraDelPlazoResponde400ConElMensajeDelDominio() throws Exception {
        given(solicitarReembolso.ejecutar(any(), any(), any(), any(), any()))
                .willThrow(new ReglaDominioException("El plazo de 48 horas para pedir el reembolso ya vencio"));

        mockMvc.perform(post("/api/reembolsos").contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El plazo de 48 horas para pedir el reembolso ya vencio"));
    }

    @Test
    void solicitarSobreUnaCompraQueNoExisteResponde404() throws Exception {
        given(solicitarReembolso.ejecutar(any(), any(), any(), any(), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(post("/api/reembolsos").contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isNotFound());
    }

    @Test
    void unVendedorNoPuedeSolicitarReembolsoYResponde403() throws Exception {
        mockMvc.perform(post("/api/reembolsos").with(token("vendedor-1", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isForbidden());

        verify(solicitarReembolso, never()).ejecutar(any(), any(), any(), any(), any());
    }

    @Test
    void solicitarSinTokenResponde401() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();

        mockMvc.perform(post("/api/reembolsos").contentType(MediaType.APPLICATION_JSON).content(SOLICITUD))
                .andExpect(status().isUnauthorized());

        verify(solicitarReembolso, never()).ejecutar(any(), any(), any(), any(), any());
    }

    // ----- aprobar -----

    @Test
    void elVendedorApruebaYResponde200ConLaFechaDeResolucion() throws Exception {
        SolicitudReembolso aprobada = solicitud();
        aprobada.aprobar("vendedor-1", SOLICITADA.plusHours(2));
        given(aprobarReembolso.ejecutar("reembolso-1", "vendedor-1")).willReturn(aprobada);

        mockMvc.perform(put("/api/reembolsos/reembolso-1/aprobar").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.fechaResolucion").exists());
    }

    @Test
    void otroVendedorNoPuedeAprobarYResponde400ConElMensajeDelDominio() throws Exception {
        given(aprobarReembolso.ejecutar("reembolso-1", "vendedor-2")).willThrow(
                new ReglaDominioException("Solo el vendedor dueno de la presentacion puede resolver su reembolso"));

        mockMvc.perform(put("/api/reembolsos/reembolso-1/aprobar").with(token("vendedor-2", "CAFICULTOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo el vendedor dueno de la presentacion puede resolver su reembolso"));
    }

    @Test
    void elCompradorNoPuedeAprobarYResponde403() throws Exception {
        mockMvc.perform(put("/api/reembolsos/reembolso-1/aprobar"))
                .andExpect(status().isForbidden());

        verify(aprobarReembolso, never()).ejecutar(any(), any());
    }

    @Test
    void aprobarUnaSolicitudQueNoExisteResponde404() throws Exception {
        given(aprobarReembolso.ejecutar(eq("no-existe"), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/reembolsos/no-existe/aprobar").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isNotFound());
    }

    // ----- rechazar -----

    @Test
    void elVendedorRechazaYResponde200() throws Exception {
        SolicitudReembolso rechazada = solicitud();
        rechazada.rechazar("vendedor-1", SOLICITADA.plusHours(2));
        given(rechazarReembolso.ejecutar("reembolso-1", "vendedor-1")).willReturn(rechazada);

        mockMvc.perform(put("/api/reembolsos/reembolso-1/rechazar").with(token("vendedor-1", "VENDEDOR_DERIVADOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADO"));
    }

    @Test
    void rechazarUnaSolicitudYaResueltaResponde400() throws Exception {
        given(rechazarReembolso.ejecutar("reembolso-1", "vendedor-1")).willThrow(
                new ReglaDominioException("Solo se puede resolver una Solicitud de Reembolso que este SOLICITADA"));

        mockMvc.perform(put("/api/reembolsos/reembolso-1/rechazar").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("SOLICITADA")));
    }

    @Test
    void rechazarUnaSolicitudQueNoExisteResponde404() throws Exception {
        given(rechazarReembolso.ejecutar(eq("no-existe"), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/reembolsos/no-existe/rechazar").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isNotFound());
    }
}
