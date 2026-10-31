package com.uniquindio.ecommerce.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El ranking de ventas es público (no pide token) y recorre todo el camino contra las Compras de ejemplo.
 * La ruta /mas-vendidas no se confunde con el detalle /{id}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RankingMasVendidasIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elRankingEsPublicoYTraeLasMasVendidasEnOrden() throws Exception {
        mockMvc.perform(get("/api/presentaciones/mas-vendidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].presentacionId").value("pres-cafe-1"))
                .andExpect(jsonPath("$[0].titulo").value("Cafe tostado Huila"))
                .andExpect(jsonPath("$[0].compras").value(2))
                .andExpect(jsonPath("$[0].ingresos").value(140000.0))
                .andExpect(jsonPath("$[1].presentacionId").value("pres-cafe-3"));
    }

    @Test
    void elLimiteRecortaElRanking() throws Exception {
        mockMvc.perform(get("/api/presentaciones/mas-vendidas").param("limite", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void unLimiteFueraDeRangoResponde400() throws Exception {
        mockMvc.perform(get("/api/presentaciones/mas-vendidas").param("limite", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El límite debe ser al menos 1"));
    }
}
