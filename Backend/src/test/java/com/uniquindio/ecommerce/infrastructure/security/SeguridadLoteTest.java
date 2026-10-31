package com.uniquindio.ecommerce.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadLoteTest {

    private static final String LOTE = """
            {
              "cantidad": { "valor": 100, "unidad": "kg" },
              "fincaId": "finca-001",
              "procesoDeBeneficio": "Lavado",
              "estado": "PERGAMINO",
              "cosecha": { "anio": %d, "temporada": "PRINCIPAL" },
              "variedad": "Caturra"
            }
            """.formatted(Year.now().getValue());

    @Autowired
    MockMvc mockMvc;

    private RequestPostProcessor usuario(String id, String rol) {
        return jwt().jwt(token -> token.subject(id).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void registrarSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/lotes")
                        .contentType(MediaType.APPLICATION_JSON).content(LOTE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarConTokenQueNoEsCaficultor403() throws Exception {
        mockMvc.perform(post("/api/lotes")
                        .with(usuario("usr-comprador", "COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(LOTE))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultarUnLoteSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/lotes/LOT-2026-002")).andExpect(status().isUnauthorized());
    }

    @Test
    void cualquierUsuarioAutenticadoPuedeConsultarUnLote() throws Exception {
        mockMvc.perform(get("/api/lotes/LOT-2026-002")
                        .with(usuario("usr-comprador", "COMPRADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void caficultorPuedeRegistrar201() throws Exception {
        mockMvc.perform(post("/api/lotes")
                        .with(usuario("usr-caficultor", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(LOTE))
                .andExpect(header().string("Location", containsString("/api/lotes/LOT-")))
                .andExpect(status().isCreated());
    }


}
