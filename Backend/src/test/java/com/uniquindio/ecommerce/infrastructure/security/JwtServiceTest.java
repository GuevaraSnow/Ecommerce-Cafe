package com.uniquindio.ecommerce.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * El token JWT lleva el correo, el rol y el vencimiento (además del id como sujeto): se decodifica con el mismo
 * decodificador que valida cada petición, tanto el que emite el servicio como el que devuelve el inicio de sesión.
 */
@SpringBootTest
@AutoConfigureMockMvc
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.jwt.duracion-minutos}")
    private long duracionEnMinutos;

    @Test
    void elTokenLlevaElIdComoSujetoElCorreoElRolYElVencimiento() {
        Instant antes = Instant.now().minusSeconds(1);

        Jwt token = jwtDecoder.decode(jwtService.generarToken("usr-1", "carlos@finca.com", Rol.CAFICULTOR));

        assertEquals("usr-1", token.getSubject());
        assertEquals("carlos@finca.com", token.getClaimAsString(JwtService.CLAIM_CORREO));
        assertEquals("CAFICULTOR", token.getClaimAsString(JwtService.CLAIM_ROL));
        assertEquals(Duration.ofMinutes(duracionEnMinutos), Duration.between(token.getIssuedAt(), token.getExpiresAt()));
        assertEquals(true, token.getIssuedAt().isAfter(antes));
    }

    @Test
    void elTokenQueDevuelveElInicioDeSesionLlevaElCorreoYElRolDelUsuario() throws Exception {
        String respuesta = mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ana@correo.com\", \"contrasena\": \"Cafe2026*\"}"))
                .andReturn().getResponse().getContentAsString();
        JsonNode cuerpo = new ObjectMapper().readTree(respuesta);

        Jwt token = jwtDecoder.decode(cuerpo.get("token").asText());

        assertEquals("usr-ana", token.getSubject());
        assertEquals("ana@correo.com", token.getClaimAsString(JwtService.CLAIM_CORREO));
        assertEquals("COMPRADOR", token.getClaimAsString(JwtService.CLAIM_ROL));
    }
}
