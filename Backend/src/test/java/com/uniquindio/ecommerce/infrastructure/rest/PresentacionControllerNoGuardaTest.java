package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.usecase.*;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.PresentacionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller + caso de uso REAL de cambiar precio, con solo el repositorio simulado: así se comprueba
 * que cuando el recurso no existe (404) o se rompe una regla de negocio (400) no se guarda nada.
 */
@WebMvcTest(PresentacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({PresentacionMapper.class, CambiarPrecioDePresentacion.class})
class PresentacionControllerNoGuardaTest {

    private static final String NUEVO_PRECIO = """
            {"precio": {"monto": 18000, "moneda": "COP"}}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PresentacionRepositorio repositorio;

    // el controlador necesita los demás casos de uso para construirse; aquí no se usan
    @MockitoBean
    private PublicarPresentacion publicarPresentacion;
    @MockitoBean
    private PublicarArticuloMerchandising publicarArticulo;
    @MockitoBean
    private ReponerStockDePresentacion reponerStock;
    @MockitoBean
    private RetirarStockDePresentacion retirarStock;
    @MockitoBean
    private PausarPresentacion pausar;
    @MockitoBean
    private ReactivarPresentacion reactivar;
    @MockitoBean
    private ReemplazarGaleriaDePresentacion reemplazarGaleria;
    @MockitoBean
    private RegistrarNotaDeCata registrarNotaDeCata;
    @MockitoBean
    private ProgramarDescuentoDePresentacion programarDescuento;
    @MockitoBean
    private QuitarDescuentoDePresentacion quitarDescuento;
    @MockitoBean
    private EliminarPresentacion eliminar;

    private Presentacion manilla() {
        return Presentacion.publicar("pres-1", "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/1.png", true))), null,
                Rol.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    @Test
    void cambiarElPrecioDeUnaPresentacionInexistenteResponde404YNoGuardaNada() throws Exception {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/presentaciones/no-existe/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NUEVO_PRECIO))
                .andExpect(status().isNotFound());

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void cambiarElPrecioDeUnaPresentacionDadaDeBajaResponde400YNoGuardaNada() throws Exception {
        Presentacion dadaDeBaja = manilla();
        dadaDeBaja.darDeBaja();
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(dadaDeBaja));

        mockMvc.perform(put("/api/presentaciones/pres-1/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NUEVO_PRECIO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede operar sobre una Presentación dada de baja"));

        verify(repositorio, never()).guardar(any());
    }

    @Test
    void cambiarElPrecioConUnCuerpoInvalidoResponde400SinSiquieraBuscarLaPresentacion() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-1/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(repositorio, never()).buscarPorId(any());
        verify(repositorio, never()).guardar(any());
    }
}
