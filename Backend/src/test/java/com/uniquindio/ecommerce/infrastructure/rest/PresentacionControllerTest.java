package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.usecase.*;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.PresentacionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba la capa web de los comandos: los casos de uso están simulados, el mapper y el manejador de errores son los reales. */
@WebMvcTest(PresentacionController.class)
@Import(PresentacionMapper.class)
class PresentacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicarPresentacion publicarPresentacion;
    @MockitoBean
    private PublicarArticuloMerchandising publicarArticulo;
    @MockitoBean
    private CambiarPrecioDePresentacion cambiarPrecio;
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

    private static final String CANTIDAD = """
            {"valor": 5, "unidad": "unidad"}""";

    private ArticuloDeMerchandising manilla() {
        return (ArticuloDeMerchandising) Presentacion.publicar("pres-1", "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                new Galeria(List.of(new ImagenDePresentacion("http://img/1.png", true))), null,
                RolVendedor.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    private String publicarMerchandisingBody() {
        return """
                {"titulo": "Manilla", "descripcion": "Manilla artesanal", "material": "semilla de café",
                 "precio": {"monto": 15000, "moneda": "COP"}, "cantidad": {"valor": 20, "unidad": "unidad"},
                 "galeria": [{"url": "http://img/1.png", "principal": true}]}""";
    }

    // ----- publicar -----

    @Test
    void publicarUnArticuloResponde201ConLocationYElCuerpo() throws Exception {
        given(publicarArticulo.ejecutar(any(), eq("vendedor-1"), eq("Manilla"), any(), any(), any(), any(), any(),
                eq(RolVendedor.VENDEDOR_DERIVADOS))).willReturn(manilla());

        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header("X-Vendedor-Id", "vendedor-1")
                        .header("X-Rol-Vendedor", "VENDEDOR_DERIVADOS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicarMerchandisingBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/presentaciones/pres-1"))
                .andExpect(jsonPath("$.id").value("pres-1"))
                .andExpect(jsonPath("$.tipo").value("MERCHANDISING"))
                .andExpect(jsonPath("$.precioVigente.monto").value(15000));
    }

    @Test
    void elIdSeGeneraEnElServidorComoUuid() throws Exception {
        given(publicarArticulo.ejecutar(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(manilla());

        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header("X-Vendedor-Id", "vendedor-1")
                        .header("X-Rol-Vendedor", "VENDEDOR_DERIVADOS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicarMerchandisingBody()))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<String> id = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(publicarArticulo).ejecutar(id.capture(), any(), any(), any(), any(), any(), any(), any(), any());
        java.util.UUID.fromString(id.getValue());
    }

    @Test
    void publicarUnaPresentacionTrazableResponde201() throws Exception {
        given(publicarPresentacion.ejecutar(any(), eq("vendedor-1"), eq("LOT-2026-001"), any(), eq("Café Verde"),
                eq(TipoDePresentacion.CAFE_VERDE), any(), any(), any(), any(), eq(RolVendedor.CAFICULTOR), any()))
                .willReturn(manilla());

        mockMvc.perform(post("/api/presentaciones")
                        .header("X-Vendedor-Id", "vendedor-1")
                        .header("X-Rol-Vendedor", "CAFICULTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Café Verde", "tipo": "CAFE_VERDE", "loteId": "LOT-2026-001",
                                 "precio": {"monto": 28000, "moneda": "COP"}, "cantidad": {"valor": 200, "unidad": "kg"},
                                 "galeria": [{"url": "http://img/1.png", "principal": true}]}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void publicarConElCuerpoIncompletoResponde400ConLosCamposYNoLlamaAlCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header("X-Vendedor-Id", "vendedor-1")
                        .header("X-Rol-Vendedor", "VENDEDOR_DERIVADOS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": " ", "precio": {"monto": -5, "moneda": "COP"}}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("titulo: El título es obligatorio")))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("precio.monto")));

        verify(publicarArticulo, never()).ejecutar(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void publicarSinElEncabezadoDelVendedorResponde400() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header("X-Rol-Vendedor", "VENDEDOR_DERIVADOS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicarMerchandisingBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el encabezado 'X-Vendedor-Id'"));
    }

    @Test
    void publicarConUnRolDesconocidoResponde400() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header("X-Vendedor-Id", "vendedor-1")
                        .header("X-Rol-Vendedor", "REY")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicarMerchandisingBody()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void siElDominioRechazaLaPublicacionResponde400ConSuMensaje() throws Exception {
        given(publicarArticulo.ejecutar(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .willThrow(new ReglaDominioException("Solo un Vendedor de Derivados puede publicar un Artículo de Merchandising"));

        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header("X-Vendedor-Id", "vendedor-1")
                        .header("X-Rol-Vendedor", "TOSTADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicarMerchandisingBody()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo un Vendedor de Derivados puede publicar un Artículo de Merchandising"));
    }

    // ----- operaciones sobre una Presentación existente -----

    @Test
    void cambiarElPrecioResponde200ConLaPresentacion() throws Exception {
        given(cambiarPrecio.ejecutar(eq("pres-1"), any())).willReturn(manilla());

        mockMvc.perform(put("/api/presentaciones/pres-1/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"precio": {"monto": 18000, "moneda": "COP"}}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pres-1"));
    }

    @Test
    void cambiarElPrecioSinPrecioResponde400() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-1/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("precio: El precio es obligatorio"));
    }

    @Test
    void cambiarElPrecioDeUnaPresentacionInexistenteResponde404() throws Exception {
        given(cambiarPrecio.ejecutar(eq("no-existe"), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/presentaciones/no-existe/precio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"precio": {"monto": 18000, "moneda": "COP"}}"""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void reponerStockResponde200() throws Exception {
        given(reponerStock.ejecutar(eq("pres-1"), any())).willReturn(manilla());

        mockMvc.perform(post("/api/presentaciones/pres-1/reposiciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": " + CANTIDAD + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void reponerStockConUnaCantidadNegativaResponde400() throws Exception {
        mockMvc.perform(post("/api/presentaciones/pres-1/reposiciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cantidad": {"valor": -3, "unidad": "kg"}}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("cantidad.valor: El valor de la cantidad debe ser mayor que cero"));
    }

    @Test
    void retirarStockResponde200() throws Exception {
        given(retirarStock.ejecutar(eq("pres-1"), any())).willReturn(manilla());

        mockMvc.perform(post("/api/presentaciones/pres-1/retiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": " + CANTIDAD + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void retirarMasStockDelDisponibleResponde400() throws Exception {
        given(retirarStock.ejecutar(eq("pres-1"), any())).willThrow(new ReglaDominioException("Stock insuficiente"));

        mockMvc.perform(post("/api/presentaciones/pres-1/retiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": " + CANTIDAD + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente"));
    }

    @Test
    void pausarYReactivarResponden200() throws Exception {
        given(pausar.ejecutar("pres-1")).willReturn(manilla());
        given(reactivar.ejecutar("pres-1")).willReturn(manilla());

        mockMvc.perform(post("/api/presentaciones/pres-1/pausa")).andExpect(status().isOk());
        mockMvc.perform(post("/api/presentaciones/pres-1/reactivacion")).andExpect(status().isOk());
    }

    @Test
    void pausarUnaPresentacionInexistenteResponde404() throws Exception {
        given(pausar.ejecutar("no-existe")).willThrow(new NoSuchElementException());

        mockMvc.perform(post("/api/presentaciones/no-existe/pausa")).andExpect(status().isNotFound());
    }

    @Test
    void pausarUnaPresentacionQueNoEstaActivaResponde400() throws Exception {
        given(pausar.ejecutar("pres-1")).willThrow(new ReglaDominioException("Solo una Presentación Activa puede pausarse"));

        mockMvc.perform(post("/api/presentaciones/pres-1/pausa"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Solo una Presentación Activa puede pausarse"));
    }

    @Test
    void reemplazarLaGaleriaResponde200() throws Exception {
        given(reemplazarGaleria.ejecutar(eq("pres-1"), any())).willReturn(manilla());

        mockMvc.perform(put("/api/presentaciones/pres-1/galeria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"galeria": [{"url": "http://img/2.png", "principal": true}]}"""))
                .andExpect(status().isOk());
    }

    @Test
    void reemplazarLaGaleriaSinGaleriaResponde400() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-1/galeria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarLaNotaDeCataResponde200() throws Exception {
        given(registrarNotaDeCata.ejecutar(eq("pres-1"), any())).willReturn(manilla());

        mockMvc.perform(put("/api/presentaciones/pres-1/nota-de-cata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descriptores": ["Chocolate", "Panela"]}"""))
                .andExpect(status().isOk());
    }

    @Test
    void programarUnDescuentoResponde200() throws Exception {
        Presentacion conDescuento = manilla();
        conDescuento.programarDescuento(new Descuento(20, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10)));
        given(programarDescuento.ejecutar(eq("pres-1"), any())).willReturn(conDescuento);

        mockMvc.perform(put("/api/presentaciones/pres-1/descuento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"porcentaje": 20, "fechaInicio": "2026-12-01", "fechaFin": "2026-12-31"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descuento.porcentaje").value(20))
                .andExpect(jsonPath("$.precio.monto").value(15000))
                .andExpect(jsonPath("$.precioVigente.monto").value(12000));
    }

    @Test
    void programarUnDescuentoSinFechasResponde400() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-1/descuento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"porcentaje": 20}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("fechaInicio")));
    }

    @Test
    void programarUnDescuentoFueraDeRangoResponde400PorElDominio() throws Exception {
        // el mapper construye el Descuento y el dominio lo rechaza antes de llegar al caso de uso
        mockMvc.perform(put("/api/presentaciones/pres-1/descuento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"porcentaje": 90, "fechaInicio": "2026-12-01", "fechaFin": "2026-12-31"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El porcentaje de descuento debe estar entre 1 y 50"));
    }

    @Test
    void quitarElDescuentoResponde200() throws Exception {
        given(quitarDescuento.ejecutar("pres-1")).willReturn(manilla());

        mockMvc.perform(delete("/api/presentaciones/pres-1/descuento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descuento").doesNotExist());
    }

    @Test
    void darDeBajaResponde204() throws Exception {
        mockMvc.perform(delete("/api/presentaciones/pres-1")).andExpect(status().isNoContent());

        verify(eliminar).ejecutar("pres-1");
    }

    @Test
    void darDeBajaUnaPresentacionInexistenteResponde404() throws Exception {
        given(eliminar.ejecutar("no-existe")).willThrow(new NoSuchElementException());

        mockMvc.perform(delete("/api/presentaciones/no-existe")).andExpect(status().isNotFound());
    }

    @Test
    void darDeBajaUnaPresentacionConComprasActivasResponde400() throws Exception {
        given(eliminar.ejecutar("pres-1")).willThrow(new ReglaDominioException("Tiene compras activas"));

        mockMvc.perform(delete("/api/presentaciones/pres-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Tiene compras activas"));
    }
}
