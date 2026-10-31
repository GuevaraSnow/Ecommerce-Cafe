package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.usecase.BuscarPresentaciones;
import com.uniquindio.ecommerce.application.usecase.ConsultarPresentacion;
import com.uniquindio.ecommerce.application.usecase.SugerirPresentaciones;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.PresentacionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba la capa web de las consultas: los casos de uso están simulados, el mapper y el manejador de errores son los reales. */
@WebMvcTest(PresentacionConsultaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(PresentacionMapper.class)
class PresentacionConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarPresentacion consultarPresentacion;
    @MockitoBean
    private BuscarPresentaciones buscarPresentaciones;
    @MockitoBean
    private SugerirPresentaciones sugerirPresentaciones;

    private Presentacion manilla() {
        return Presentacion.publicar("pres-1", "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/1.png", true))), null,
                Rol.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    // ----- detalle -----

    @Test
    void elDetalleDevuelveLaPresentacionConSuGaleria() throws Exception {
        given(consultarPresentacion.ejecutar("pres-1")).willReturn(manilla());

        mockMvc.perform(get("/api/presentaciones/pres-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pres-1"))
                .andExpect(jsonPath("$.titulo").value("Manilla"))
                .andExpect(jsonPath("$.galeria[0].url").value("http://img/1.png"))
                .andExpect(jsonPath("$.galeria[0].principal").value(true))
                .andExpect(jsonPath("$.precioVigente.monto").value(15000));
    }

    @Test
    void elDetalleDeUnaPresentacionInexistenteResponde404() throws Exception {
        given(consultarPresentacion.ejecutar("no-existe")).willThrow(new NoSuchElementException());

        mockMvc.perform(get("/api/presentaciones/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"));
    }

    // ----- búsqueda con filtros y paginación -----

    @Test
    void buscarSinFiltrosUsaLaPrimeraPaginaDeDiez() throws Exception {
        given(buscarPresentaciones.ejecutar(any(), anyInt(), anyInt()))
                .willReturn(new Pagina<>(List.of(manilla()), 0, 10, 1));

        mockMvc.perform(get("/api/presentaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value("pres-1"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamano").value(10))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));

        verify(buscarPresentaciones).ejecutar(new FiltroDePresentaciones(null, null, null, null, null), 0, 10);
    }

    @Test
    void buscarPasaLosFiltrosYLaPaginacionAlCasoDeUso() throws Exception {
        given(buscarPresentaciones.ejecutar(any(), anyInt(), anyInt()))
                .willReturn(new Pagina<>(List.of(), 2, 5, 0));

        mockMvc.perform(get("/api/presentaciones")
                        .param("titulo", "café")
                        .param("tipo", "CAFE_TOSTADO")
                        .param("vendedorId", "vendedor-1")
                        .param("precioMinimo", "10000")
                        .param("precioMaximo", "40000")
                        .param("calificacionMinima", "4")
                        .param("pagina", "2")
                        .param("tamano", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<FiltroDePresentaciones> filtro = ArgumentCaptor.forClass(FiltroDePresentaciones.class);
        verify(buscarPresentaciones).ejecutar(filtro.capture(), eq(2), eq(5));
        assertEquals("café", filtro.getValue().titulo());
        assertEquals(TipoDePresentacion.CAFE_TOSTADO, filtro.getValue().tipo());
        assertEquals("vendedor-1", filtro.getValue().vendedorId());
        assertEquals(10000.0, filtro.getValue().precioMinimo());
        assertEquals(40000.0, filtro.getValue().precioMaximo());
        assertEquals(4.0, filtro.getValue().calificacionMinima());
    }

    @Test
    void buscarConUnaCalificacionMinimaFueraDeRangoResponde400PorElDominio() throws Exception {
        mockMvc.perform(get("/api/presentaciones").param("calificacionMinima", "6"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La calificación mínima debe estar entre 1 y 5: 6.0"));

        verify(buscarPresentaciones, never()).ejecutar(any(), anyInt(), anyInt());
    }

    @Test
    void buscarConUnTamanoDePaginaFueraDeRangoResponde400() throws Exception {
        mockMvc.perform(get("/api/presentaciones").param("tamano", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El tamaño de página no puede superar 50"));
        mockMvc.perform(get("/api/presentaciones").param("tamano", "0"))
                .andExpect(status().isBadRequest());

        verify(buscarPresentaciones, never()).ejecutar(any(), anyInt(), anyInt());
    }

    @Test
    void buscarConUnaPaginaNegativaResponde400() throws Exception {
        mockMvc.perform(get("/api/presentaciones").param("pagina", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La página no puede ser negativa"));
    }

    @Test
    void buscarConUnTipoInexistenteResponde400() throws Exception {
        mockMvc.perform(get("/api/presentaciones").param("tipo", "TE_VERDE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'tipo' tiene un valor no válido"));
    }

    @Test
    void buscarConElPrecioMinimoMayorQueElMaximoResponde400PorElDominio() throws Exception {
        mockMvc.perform(get("/api/presentaciones").param("precioMinimo", "50000").param("precioMaximo", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El precio mínimo no puede ser mayor que el máximo"));
    }

    // ----- búsqueda predictiva -----

    @Test
    void lasSugerenciasDevuelvenIdYTitulo() throws Exception {
        given(sugerirPresentaciones.ejecutar("man", 5)).willReturn(List.of(manilla()));

        mockMvc.perform(get("/api/presentaciones/sugerencias").param("texto", "man"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("pres-1"))
                .andExpect(jsonPath("$[0].titulo").value("Manilla"))
                .andExpect(jsonPath("$[0].precio").doesNotExist());
    }

    @Test
    void lasSugerenciasSinTextoOConTextoEnBlancoResponden400() throws Exception {
        mockMvc.perform(get("/api/presentaciones/sugerencias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro 'texto'"));
        mockMvc.perform(get("/api/presentaciones/sugerencias").param("texto", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El texto de búsqueda es obligatorio"));
    }

    @Test
    void lasSugerenciasConUnLimiteFueraDeRangoResponden400() throws Exception {
        mockMvc.perform(get("/api/presentaciones/sugerencias").param("texto", "man").param("limite", "50"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El límite no puede superar 10"));
    }
}
