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
 * La seguridad real de las Transformaciones, con los datos de ejemplo: registrar exige ser Tostador y la
 * Transformación queda a nombre del usuario del token; la Ficha de Origen exige estar autenticado.
 * LOT-2026-001 tiene 205 kg y trans-001 es de usr-tostador.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadTransformacionTest {

    private static final String TRANSFORMACION = """
            {"loteCodigo": "LOT-2026-001", "cantidadEntrada": {"valor": 20.0, "unidad": "kg"}, "merma": 0.15,
             "fecha": "2026-10-28", "perfilTuesteResultante": "MEDIO"}""";

    @Autowired
    private MockMvc mockMvc;

    private RequestPostProcessor usuario(String id, String rol) {
        return jwt().jwt(token -> token.subject(id).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    @Test
    void registrarSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/transformaciones").contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unCaficultorNoPuedeRegistrarUnaTransformacion() throws Exception {
        mockMvc.perform(post("/api/transformaciones").with(usuario("usr-caficultor", "CAFICULTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isForbidden());
    }

    @Test
    void unTostadorRegistraUnaTransformacionQueQuedaASuNombreYDescuentaElLote() throws Exception {
        mockMvc.perform(post("/api/transformaciones").with(usuario("usr-tostador", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRANSFORMACION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tostadorId").value("usr-tostador"))
                .andExpect(jsonPath("$.cantidadResultante.valor").value(17.0));

        mockMvc.perform(get("/api/lotes/LOT-2026-001").with(usuario("usr-ana", "COMPRADOR")))
                .andExpect(jsonPath("$.cantidadDisponible.valor").value(185.0));
    }

    @Test
    void registrarSobreUnLoteQueNoExisteResponde404() throws Exception {
        mockMvc.perform(post("/api/transformaciones").with(usuario("usr-tostador", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRANSFORMACION.replace("LOT-2026-001", "LOT-2026-999")))
                .andExpect(status().isNotFound());
    }

    @Test
    void laFichaDeOrigenDeEjemploSeConsultaConCualquierRolAutenticado() throws Exception {
        mockMvc.perform(get("/api/transformaciones/trans-001/ficha-origen").with(usuario("usr-ana", "COMPRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fincaNombre").value("Finca Villa Laura"))
                .andExpect(jsonPath("$.cosechaInfo").value("Año: 2026 - Temporada: PRINCIPAL"))
                .andExpect(jsonPath("$.tostadorId").value("usr-tostador"));
    }

    @Test
    void laFichaDeUnaTransformacionInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/transformaciones/no-existe/ficha-origen").with(usuario("usr-ana", "COMPRADOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void laFichaSinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/transformaciones/trans-001/ficha-origen")).andExpect(status().isUnauthorized());
    }
}
