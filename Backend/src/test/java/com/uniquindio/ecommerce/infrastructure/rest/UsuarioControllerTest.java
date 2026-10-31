package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.dto.request.EditarPerfilRequest;
import com.uniquindio.ecommerce.application.usecase.*;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.UsuarioMapper;
import com.uniquindio.ecommerce.infrastructure.security.JwtService;
import com.uniquindio.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtService.class, UsuarioMapper.class})
@TestPropertySource(properties = {
        "app.jwt.secret=12345678901234567890123456789012",
        "app.jwt.duracion-minutos=15"
})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Usuario usuarioPrueba() {
        return Usuario.registrar(
                "usr-123",
                "Joseph Cortes",
                new Email("joseph@uniquindio.edu.co"),
                new Contrasena("HashedPass123!"),
                new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(2000, 1, 1)),
                Rol.CAFICULTOR
        );
    }

    @Test
    void registroExitosoResponde201() throws Exception {
        given(registrarUsuario.ejecutar(any(), any(), any(), any(), any(), any()))
                .willReturn(usuarioPrueba());

        String jsonBody = """
                {
                    "nombre": "Joseph Cortes",
                    "email": "joseph@uniquindio.edu.co",
                    "contrasena": "Password123!",
                    "telefono": "3001234567",
                    "fechaNacimiento": "2000-01-01",
                    "rol": "CAFICULTOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value("usr-123"))
                .andExpect(jsonPath("$.email").value("joseph@uniquindio.edu.co"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    void registroConEmailDuplicadoResponde400() throws Exception {
        given(registrarUsuario.ejecutar(any(), any(), any(), any(), any(), any()))
                .willThrow(new ReglaDominioException("El email ya se encuentra registrado."));

        String jsonBody = """
                {
                    "nombre": "Joseph Cortes",
                    "email": "duplicado@uniquindio.edu.co",
                    "contrasena": "Password123!",
                    "fechaNacimiento": "2000-01-01",
                    "rol": "CAFICULTOR"
                }
                """;

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El email ya se encuentra registrado."));
    }

    @Test
    void loginExitosoResponde200() throws Exception {
        given(iniciarSesion.ejecutar(any(), any()))
                .willReturn(usuarioPrueba());

        String jsonBody = """
                {
                    "email": "joseph@uniquindio.edu.co",
                    "contrasena": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.id").value("usr-123"))
                .andExpect(jsonPath("$.usuario.contrasena").doesNotExist());
    }

    @Test
    void editarPerfilResponde200ConEncabezado() throws Exception {
        given(editarPerfil.ejecutar(eq("usr-123"), any(EditarPerfilRequest.class)))
                .willReturn(usuarioPrueba());

        String jsonBody = """
                {
                    "nombre": "Joseph Cortes Editado",
                    "telefono": "3119876543"
                }
                """;

        mockMvc.perform(put("/api/usuarios/perfil")
                        .with(jwt().jwt(j -> j.subject("usr-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-123"));
    }

    @Test
    void cambiarContrasenaExitosoResponde200() throws Exception {
        doNothing().when(cambiarContrasena).ejecutar(eq("usr-123"), any(), any());

        String jsonBody = """
                {
                    "contrasenaActual": "OldPass123!",
                    "nuevaContrasena": "NewPass123!"
                }
                """;

        mockMvc.perform(put("/api/usuarios/perfil/contrasena")
                        .with(jwt().jwt(j -> j.subject("usr-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarContrasenaIncorrectaResponde400() throws Exception {
        doThrow(new ReglaDominioException("La contraseña actual es incorrecta."))
                .when(cambiarContrasena).ejecutar(eq("usr-123"), any(), any());

        String jsonBody = """
                {
                    "contrasenaActual": "WrongPass123!",
                    "nuevaContrasena": "NewPass123!"
                }
                """;

        mockMvc.perform(put("/api/usuarios/perfil/contrasena")
                        .with(jwt().jwt(j -> j.subject("usr-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La contraseña actual es incorrecta."));
    }
}