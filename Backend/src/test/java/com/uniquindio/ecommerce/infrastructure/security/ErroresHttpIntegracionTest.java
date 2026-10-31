package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import com.uniquindio.ecommerce.infrastructure.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los errores de toda la API tienen el mismo cuerpo JSON (timestamp, status y mensaje), también los que salen del
 * filtro de seguridad (401 y 403), con la seguridad y el manejador reales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ErroresHttpIntegracionTest {

    private static final String LOTE_CON_CANTIDAD_NEGATIVA = """
            {"cantidad": {"valor": -5, "unidad": "kg"}, "fincaId": "finca-001", "procesoDeBeneficio": "Lavado",
             "estado": "PERGAMINO", "cosecha": {"anio": 2026, "temporada": "PRINCIPAL"}, "variedad": "Caturra"}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private RequestPostProcessor usuario(String id, String rol) {
        return jwt().jwt(token -> token.subject(id).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void sinTokenResponde401ConElCuerpoJsonDeError() throws Exception {
        mockMvc.perform(get("/api/lotes/LOT-2026-001"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value(GlobalExceptionHandler.MENSAJE_AUTENTICACION_REQUERIDA))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unTokenVencidoResponde401ConElMismoCuerpoJson() throws Exception {
        String vencido = jwtService.generarToken("usr-ana", "ana@correo.com", Rol.COMPRADOR,
                Instant.now().minusSeconds(2 * 60 * 60));

        mockMvc.perform(get("/api/lotes/LOT-2026-001").header(HttpHeaders.AUTHORIZATION, "Bearer " + vencido))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value(GlobalExceptionHandler.MENSAJE_AUTENTICACION_REQUERIDA));
    }

    @Test
    void unRolSinPermisoResponde403ConElCuerpoJsonDeError() throws Exception {
        mockMvc.perform(post("/api/lotes").with(usuario("usr-ana", "COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(LOTE_CON_CANTIDAD_NEGATIVA))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value(GlobalExceptionHandler.MENSAJE_SIN_PERMISO));
    }

    @Test
    void unaReglaDeNegocioRotaResponde422ConSuMensaje() throws Exception {
        mockMvc.perform(post("/api/lotes").with(usuario("usr-caficultor", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(LOTE_CON_CANTIDAD_NEGATIVA))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensaje").value("La Cantidad no puede ser negativa: -5.0"));
    }

    @Test
    void unaRutaQueNoExisteConTokenResponde404ConElCuerpoJsonDeError() throws Exception {
        mockMvc.perform(get("/api/ruta-que-no-existe").with(usuario("usr-ana", "COMPRADOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"));
    }

    @Test
    void unaRutaQueNoExisteSinTokenResponde401PorqueElFiltroRespondeAntesQueElControlador() throws Exception {
        mockMvc.perform(get("/api/ruta-que-no-existe"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void unMetodoNoPermitidoResponde405ConElCuerpoJsonDeError() throws Exception {
        mockMvc.perform(put("/api/lotes").with(usuario("usr-caficultor", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void laRutaDeErroresNoExigeToken() throws Exception {
        int estado = mockMvc.perform(get("/error")).andReturn().getResponse().getStatus();

        assertNotEquals(401, estado, "/error debe estar permitida: si no, un 404 o un 500 reales se verían como 401");
    }
}
