package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import com.uniquindio.ecommerce.application.usecase.*;
import com.uniquindio.ecommerce.infrastructure.rest.UsuarioController;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.UsuarioMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtService.class, UsuarioMapper.class})
@TestPropertySource(properties = {
        "app.jwt.secret=12345678901234567890123456789012",
        "app.jwt.duracion-minutos=15"
})
class SeguridadJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RegistrarUsuario registrarUsuario;

    @MockitoBean
    private IniciarSesion iniciarSesion;

    @MockitoBean
    private RecuperarContrasena recuperarContrasena;

    @MockitoBean
    private RestablecerContrasena restablecerContrasena;

    @MockitoBean
    private EditarPerfil editarPerfil;

    @MockitoBean
    private CambiarContrasena cambiarContrasena;

    @Test
    void peticionSinTokenResponde401Unauthorized() throws Exception {
        mockMvc.perform(put("/api/usuarios/perfil")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nombre Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void peticionConTokenVencidoResponde401Unauthorized() throws Exception {
        Instant tiempoPasado = Instant.now().minusSeconds(3600);
        String tokenExpirado = jwtService.generarToken("usr-test", "test@correo.com", Rol.CAFICULTOR, tiempoPasado);

        mockMvc.perform(put("/api/usuarios/perfil")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenExpirado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nombre Test\"}"))
                .andExpect(status().isUnauthorized());
    }
}