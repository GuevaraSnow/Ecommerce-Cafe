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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La seguridad real de las Fincas, con los datos de ejemplo: registrar exige ser Caficultor y la Finca queda a
 * nombre del usuario del token; consultar exige estar autenticado. finca-001 es de usr-caficultor.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadFincaTest {

    private static final String FINCA = """
            {"nombre": "Finca La Esperanza", "ubicacion": {"departamento": "Quindío", "municipio": "Montenegro"}, "altitud": 1400}""";

    @Autowired
    private MockMvc mockMvc;

    private RequestPostProcessor usuario(String id, String rol) {
        return jwt().jwt(token -> token.subject(id).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void registrarSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/fincas").contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unCompradorNoPuedeRegistrarUnaFinca() throws Exception {
        mockMvc.perform(post("/api/fincas").with(usuario("usr-ana", "COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isForbidden());
    }

    @Test
    void unCaficultorRegistraUnaFincaQueQuedaASuNombre() throws Exception {
        mockMvc.perform(post("/api/fincas").with(usuario("usr-caficultor", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(FINCA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.propietarioId").value("usr-caficultor"))
                .andExpect(jsonPath("$.nombre").value("Finca La Esperanza"));
    }

    @Test
    void cualquierUsuarioAutenticadoConsultaUnaFincaDeEjemplo() throws Exception {
        mockMvc.perform(get("/api/fincas/finca-001").with(usuario("usr-ana", "COMPRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propietarioId").value("usr-caficultor"));
    }

    @Test
    void consultarSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/fincas/finca-001")).andExpect(status().isUnauthorized());
    }

    @Test
    void consultarUnaFincaInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/fincas/no-existe").with(usuario("usr-ana", "COMPRADOR")))
                .andExpect(status().isNotFound());
    }
}
