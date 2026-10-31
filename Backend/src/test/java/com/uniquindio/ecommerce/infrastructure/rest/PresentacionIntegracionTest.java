package com.uniquindio.ecommerce.infrastructure.rest;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración: la petición entra por el controlador y recorre el caso de uso, el
 * Domain Service, el mapper, el adaptador JPA y la base de datos H2 con los datos de
 * data-presentacion.sql. No hay ningún {@code @MockitoBean}. Cada prueba deshace lo que guarda.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PresentacionIntegracionTest {

    private static final String ARTICULO = """
            {"titulo": "Posavasos", "descripcion": "Posavasos de corcho", "material": "corcho",
             "precio": {"monto": 9000, "moneda": "COP"}, "cantidad": {"valor": 30, "unidad": "unidad"},
             "galeria": [{"url": "http://img/p.png", "principal": true}]}""";

    @Autowired
    private MockMvc mockMvc;

    /** Un usuario autenticado: el sujeto del token es su id y "rol" su Rol. */
    private RequestPostProcessor vendedor(String id, String rol) {
        return jwt().jwt(token -> token.subject(id).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    /** El dueño de las presentaciones de ejemplo pres-taza-1, pres-pausada, pres-agotada y pres-eliminada. */
    private RequestPostProcessor duenoDeLaTaza() {
        return vendedor("usr-derivados", "VENDEDOR_DERIVADOS");
    }

    /** Publica el artículo por HTTP y devuelve su id, leído del Location. */
    private String publicarArticulo() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/presentaciones/merchandising")
                        .with(vendedor("usr-derivados", "VENDEDOR_DERIVADOS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ARTICULO))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.id");
    }

    @Test
    void unArticuloPublicadoPorHttpQuedaGuardadoYSePuedeConsultar() throws Exception {
        String id = publicarArticulo();

        mockMvc.perform(get("/api/presentaciones/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Posavasos"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.precio.monto").value(9000))
                .andExpect(jsonPath("$.cantidadDisponible.valor").value(30))
                .andExpect(jsonPath("$.galeria[0].url").value("http://img/p.png"))
                .andExpect(jsonPath("$.material").value("corcho"));
    }

    @Test
    void elCatalogoDelSistemaTrae4PresentacionesDeEjemploYUnaNuevaSeSumaAEllas() throws Exception {
        mockMvc.perform(get("/api/presentaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.contenido[0].id").value("pres-cafe-1"));

        String id = publicarArticulo();

        mockMvc.perform(get("/api/presentaciones"))
                .andExpect(jsonPath("$.totalElementos").value(5))
                .andExpect(jsonPath("$.contenido[*].id", hasItem(id)));
    }

    @Test
    void laBusquedaFiltraYPaginaSobreLosDatosReales() throws Exception {
        mockMvc.perform(get("/api/presentaciones").param("tipo", "CAFE_TOSTADO").param("tamano", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(1))
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.contenido[0].id").value("pres-cafe-1"));
    }

    @Test
    void pausarUnaPresentacionLaSacaDelCatalogoYReactivarlaLaDevuelve() throws Exception {
        String id = publicarArticulo();

        mockMvc.perform(post("/api/presentaciones/" + id + "/pausa").with(duenoDeLaTaza())).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));
        mockMvc.perform(get("/api/presentaciones/sugerencias").param("texto", "posavasos"))
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/presentaciones/" + id + "/reactivacion").with(duenoDeLaTaza())).andExpect(status().isOk());
        mockMvc.perform(get("/api/presentaciones/sugerencias").param("texto", "posavasos"))
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void cambiarElPrecioYProgramarUnDescuentoSeGuardanYSeVenEnLaConsulta() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-taza-1/precio")
                        .with(duenoDeLaTaza())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"precio": {"monto": 20000, "moneda": "COP"}}"""))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/presentaciones/pres-taza-1/descuento")
                        .with(duenoDeLaTaza())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"porcentaje": 10, "fechaInicio": "2020-01-01", "fechaFin": "2099-12-31"}"""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/presentaciones/pres-taza-1"))
                .andExpect(jsonPath("$.precio.monto").value(20000))
                .andExpect(jsonPath("$.precioVigente.monto").value(18000))
                .andExpect(jsonPath("$.descuento.porcentaje").value(10));
    }

    @Test
    void darDeBajaUnaPresentacionLaSacaDelCatalogoYSuDetalleResponde404() throws Exception {
        mockMvc.perform(delete("/api/presentaciones/pres-taza-1").with(duenoDeLaTaza())).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/presentaciones").param("titulo", "taza"))
                .andExpect(jsonPath("$.totalElementos").value(0));
        mockMvc.perform(get("/api/presentaciones/pres-taza-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void elDetalleDeLaPresentacionEliminadaDeLosDatosDeEjemploResponde404() throws Exception {
        mockMvc.perform(get("/api/presentaciones/pres-eliminada"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unaEscrituraSobreUnaPresentacionDadaDeBajaSigueCargandolaYSeRechazaPorLaReglaDeNegocio() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-eliminada/precio")
                        .with(duenoDeLaTaza())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"precio": {"monto": 20000, "moneda": "COP"}}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("No se puede operar sobre una Presentación dada de baja"));
    }

    @Test
    void elCatalogoNoMuestraLaVencidaNiLaPausadaNiLaAgotadaNiLaEliminada() throws Exception {
        mockMvc.perform(get("/api/presentaciones").param("tamano", "50"))
                .andExpect(jsonPath("$.contenido[*].id", not(hasItem("pres-vencida"))))
                .andExpect(jsonPath("$.contenido[*].id", not(hasItem("pres-pausada"))))
                .andExpect(jsonPath("$.contenido[*].id", not(hasItem("pres-agotada"))))
                .andExpect(jsonPath("$.contenido[*].id", not(hasItem("pres-eliminada"))));
    }

    @Test
    void consultarUnaPresentacionInexistenteResponde404() throws Exception {
        mockMvc.perform(get("/api/presentaciones/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void publicarConUnRolQueNoPuedePublicarMerchandisingResponde422YNoGuardaNada() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .with(vendedor("usr-tostador", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ARTICULO))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(get("/api/presentaciones").param("titulo", "posavasos"))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }
}
