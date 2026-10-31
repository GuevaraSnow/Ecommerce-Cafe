package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El inicio de sesión no revela por qué falla: un correo que no existe, una contraseña errada y un usuario
 * dado de baja responden lo mismo (401 y el mismo mensaje). Usa los usuarios de ejemplo (contraseña Cafe2026*).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AutenticacionIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    private ResultActions iniciarSesion(String email, String contrasena) throws Exception {
        return mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\", \"contrasena\": \"" + contrasena + "\"}"));
    }

    private void esperarCredencialesInvalidas(ResultActions respuesta) throws Exception {
        respuesta.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas."));
    }

    @Test
    void unCorreoQueNoExisteResponde401() throws Exception {
        esperarCredencialesInvalidas(iniciarSesion("nadie@correo.com", "Cafe2026*"));
    }

    @Test
    void unaContrasenaErradaResponde401() throws Exception {
        esperarCredencialesInvalidas(iniciarSesion("ana@correo.com", "Equivocada123"));
    }

    @Test
    void unUsuarioDadoDeBajaResponde401IgualQueUnaContrasenaErrada() throws Exception {
        Usuario bruno = usuarioRepositorio.buscarPorId("usr-bruno").orElseThrow();
        bruno.eliminarLogicamente();
        usuarioRepositorio.guardar(bruno);

        esperarCredencialesInvalidas(iniciarSesion("bruno@correo.com", "Cafe2026*"));
    }
}
