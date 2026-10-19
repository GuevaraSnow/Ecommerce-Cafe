package com.uniquindio.ecommerce.infrastructure.rest.exception;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba el manejador con un controlador de mentira que lanza cada excepción, sin levantar Spring. */
class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void unaReglaDeDominioVioladaResponde400ConSuMensaje() throws Exception {
        mockMvc.perform(get("/prueba/regla"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("La cantidad no puede ser negativa"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unRecursoInexistenteResponde404SinExponerElDetalleInterno() throws Exception {
        mockMvc.perform(get("/prueba/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unCuerpoQueNoPasaLaValidacionResponde400ConElCampoYSuMensaje() throws Exception {
        mockMvc.perform(post("/prueba/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("titulo: El título es obligatorio"));
    }

    @Test
    void unCuerpoQueNoEsJsonValidoResponde400() throws Exception {
        mockMvc.perform(post("/prueba/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición no es válido"));
    }

    @RestController
    @RequestMapping("/prueba")
    static class ControladorDePrueba {

        @GetMapping("/regla")
        void regla() {
            throw new ReglaDominioException("La cantidad no puede ser negativa");
        }

        @GetMapping("/no-existe")
        void noExiste() {
            throw new NoSuchElementException("No value present");
        }

        @PostMapping("/validar")
        void validar(@Valid @RequestBody CuerpoDePrueba cuerpo) {
        }
    }

    record CuerpoDePrueba(@NotBlank(message = "El título es obligatorio") String titulo) {
    }
}
