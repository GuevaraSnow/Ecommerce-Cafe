package com.uniquindio.ecommerce.infrastructure.rest.exception;

import com.uniquindio.ecommerce.Domain.exception.CredencialesInvalidasException;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
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
    void unaReglaDeDominioVioladaResponde422ConSuMensaje() throws Exception {
        mockMvc.perform(get("/prueba/regla"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensaje").value("La cantidad no puede ser negativa"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unErrorInesperadoResponde500ConUnMensajeGenericoSinExponerElDetalle() throws Exception {
        mockMvc.perform(get("/prueba/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado. Intenta de nuevo más tarde."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void lasCredencialesInvalidasResponden401() throws Exception {
        mockMvc.perform(get("/prueba/credenciales"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas."));
    }

    @Test
    void unAccesoDenegadoPorLaSeguridadDeMetodoResponde403NoLoTapaElErrorInesperado() throws Exception {
        mockMvc.perform(get("/prueba/prohibido"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value(GlobalExceptionHandler.MENSAJE_SIN_PERMISO));
    }

    @Test
    void unMetodoNoPermitidoResponde405() throws Exception {
        mockMvc.perform(post("/prueba/regla"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.mensaje").value("El método POST no está permitido en esta ruta"));
    }

    @Test
    void unTipoDeContenidoNoSoportadoResponde415() throws Exception {
        mockMvc.perform(post("/prueba/validar").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
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

    @Test
    void unParametroConUnTipoInvalidoResponde400ConElNombreDelParametro() throws Exception {
        mockMvc.perform(get("/prueba/numero").param("cantidad", "mucho"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'cantidad' tiene un valor no válido"));
    }

    @Test
    void unParametroObligatorioFaltanteResponde400() throws Exception {
        mockMvc.perform(get("/prueba/numero"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro 'cantidad'"));
    }

    @Test
    void unEncabezadoObligatorioFaltanteResponde400() throws Exception {
        mockMvc.perform(get("/prueba/encabezado"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el encabezado 'X-Prueba'"));
    }

    @Test
    void unParametroQueNoPasaLaValidacionResponde400ConSuMensaje() throws Exception {
        mockMvc.perform(get("/prueba/limitado").param("tamano", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El tamaño máximo es 50"));
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

        @GetMapping("/inesperado")
        void inesperado() {
            throw new IllegalStateException("detalle interno que no debe llegar al cliente");
        }

        @GetMapping("/credenciales")
        void credenciales() {
            throw new CredencialesInvalidasException();
        }

        @GetMapping("/prohibido")
        void prohibido() {
            throw new AccessDeniedException("sin permiso");
        }

        @GetMapping("/numero")
        void numero(@RequestParam int cantidad) {
        }

        @GetMapping("/encabezado")
        void encabezado(@RequestHeader("X-Prueba") String valor) {
        }

        @GetMapping("/limitado")
        void limitado(@RequestParam @Max(value = 50, message = "El tamaño máximo es 50") int tamano) {
        }

        @PostMapping("/validar")
        void validar(@Valid @RequestBody CuerpoDePrueba cuerpo) {
        }
    }

    record CuerpoDePrueba(@NotBlank(message = "El título es obligatorio") String titulo) {
    }
}
