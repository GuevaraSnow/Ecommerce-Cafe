package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.usecase.ConsultarLote;
import com.uniquindio.ecommerce.application.usecase.EliminarLote;
import com.uniquindio.ecommerce.application.usecase.RegistrarLote;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.LoteMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Year;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Capa web de Lote: los casos de uso están simulados; el mapper y el manejador de errores son los reales. */
@WebMvcTest(LoteController.class)
@Import(LoteMapper.class)
class LoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarLote consultarLote;

    @MockitoBean
    private RegistrarLote registrarLote;

    @MockitoBean
    private EliminarLote eliminarLote;

    private Lote loteDePrueba(CodigoDeLote codigo) {
        return Lote.registrar(
                codigo, new Cantidad(100, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));
    }

    @Test
    void getDevuelve200YElLote() throws Exception {
        CodigoDeLote codigoDeLote = CodigoDeLote.crear(Year.now().getValue(),1);
        Lote lote = loteDePrueba(codigoDeLote);

        given(consultarLote.ejecutar(codigoDeLote)).willReturn(lote);

        mockMvc.perform(get("/api/lotes/" + codigoDeLote.valor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(codigoDeLote.valor()))
                .andExpect(jsonPath("$.fincaId").value("finca-1"));
    }

    @Test
    void postDevuelve201YElLote() throws Exception {
        CodigoDeLote codigoDeLote = CodigoDeLote.crear(Year.now().getValue(),1);
        given(registrarLote.ejecutar(any(),any(),any(),any(),any(),any())).willReturn(loteDePrueba(codigoDeLote));


        String jsonValido = """
                {
                  "cantidad": { "valor": 100, "unidad": "kg" },
                  "fincaId": "finca-1",
                  "procesoDeBeneficio": "Lavado",
                  "estado": "PERGAMINO",
                  "cosecha": { "anio": %d, "temporada": "PRINCIPAL" },
                  "variedad": "Caturra"
                }
                """.formatted(Year.now().getValue());

        mockMvc.perform(post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonValido))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/lotes/" + codigoDeLote.valor())))
                .andExpect(jsonPath("$.codigo").value(codigoDeLote.valor()))
                .andExpect(jsonPath("$.fincaId").value("finca-1"));

        verify(registrarLote).ejecutar(eq(new Cantidad(100, "kg")),eq("finca-1"),eq(ProcesoDeBeneficio.Lavado),
                eq(EstadoDelCafe.PERGAMINO), eq(new Cosecha( Year.now().getValue() , TemporadaDeCosecha.PRINCIPAL)),
                eq(new VariedadDeCafe("Caturra")));
    }

    @Test
    void getCodigoValidoLoteNoExisteDevuelve404() throws Exception {
        CodigoDeLote codigoDeLote = CodigoDeLote.crear(Year.now().getValue(),1);

        given(consultarLote.ejecutar(codigoDeLote)).willThrow(new NoSuchElementException());

        mockMvc.perform(get("/api/lotes/" + codigoDeLote.valor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"));
    }

    @Test
    void getCodigoInvalidoLoteDevuelve422() throws Exception {
        mockMvc.perform(get("/api/lotes/ABC"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensaje").value("Código de Lote inválido: ABC"));

        verify(consultarLote, never()).ejecutar(any());
    }

    @Test
    void postSinUnCampoObligatorioDevuelve400() throws Exception {
        String jsonSinFinca = """
                {
                  "cantidad": { "valor": 100, "unidad": "kg" },
                  "procesoDeBeneficio": "Lavado",
                  "estado": "PERGAMINO",
                  "cosecha": { "anio": %d, "temporada": "PRINCIPAL" },
                  "variedad": "Caturra"
                }
                """.formatted(Year.now().getValue());

        mockMvc.perform(post("/api/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSinFinca))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("fincaId: La finca es obligatoria"));

        verify(registrarLote, never()).ejecutar(any(),any(),any(),any(),any(),any());
    }

    @Test
    void postUnCampoRompeUnaReglaDeNegocioDevuelve422() throws Exception {
        String jsonCantidadNegativa = """
                {
                  "cantidad": { "valor": -100, "unidad": "kg" },
                  "fincaId": "finca-1",
                  "procesoDeBeneficio": "Lavado",
                  "estado": "PERGAMINO",
                  "cosecha": { "anio": %d, "temporada": "PRINCIPAL" },
                  "variedad": "Caturra"
                }
                """.formatted(Year.now().getValue());

        mockMvc.perform(post("/api/lotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCantidadNegativa))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensaje").value("La Cantidad no puede ser negativa: -100.0"));

        verify(registrarLote, never()).ejecutar(any(),any(),any(),any(),any(),any());
    }

    @Test
    void deleteDaDeBajaElLoteYResponde204() throws Exception {
        CodigoDeLote codigoDeLote = CodigoDeLote.crear(Year.now().getValue(), 1);

        mockMvc.perform(delete("/api/lotes/" + codigoDeLote.valor()))
                .andExpect(status().isNoContent());

        verify(eliminarLote).ejecutar(codigoDeLote);
    }

    @Test
    void deleteDeUnLoteInexistenteDevuelve404() throws Exception {
        CodigoDeLote codigoDeLote = CodigoDeLote.crear(Year.now().getValue(), 1);
        given(eliminarLote.ejecutar(codigoDeLote)).willThrow(new NoSuchElementException());

        mockMvc.perform(delete("/api/lotes/" + codigoDeLote.valor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"));
    }

    @Test
    void deleteConUnCodigoInvalidoDevuelve422SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(delete("/api/lotes/ABC"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.mensaje").value("Código de Lote inválido: ABC"));

        verify(eliminarLote, never()).ejecutar(any());
    }
}
