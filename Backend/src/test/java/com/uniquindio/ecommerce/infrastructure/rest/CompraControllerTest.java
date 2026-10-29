package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.application.usecase.AgregarAlCarrito;
import com.uniquindio.ecommerce.application.usecase.CancelarCompra;
import com.uniquindio.ecommerce.application.usecase.ConfirmarCompra;
import com.uniquindio.ecommerce.application.usecase.CrearCarrito;
import com.uniquindio.ecommerce.application.usecase.MarcarEntregada;
import com.uniquindio.ecommerce.application.usecase.MarcarEnviada;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.CompraMapper;
import com.uniquindio.ecommerce.infrastructure.security.AutorizacionDeCompra;
import com.uniquindio.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comandos de CompraController: códigos HTTP (201, 200, 400, 403, 404) y que el controlador solo delega.
 * La regla de propiedad se simula aprobada; se prueba en AutorizacionDeCompraTest.
 */
@WebMvcTest(CompraController.class)
@Import({CompraMapper.class, SecurityConfig.class})
class CompraControllerTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @MockitoBean(name = "autorizacionDeCompra")
    private AutorizacionDeCompra autorizacion;

    @MockitoBean
    private CrearCarrito crearCarrito;
    @MockitoBean
    private AgregarAlCarrito agregarAlCarrito;
    @MockitoBean
    private ConfirmarCompra confirmarCompra;
    @MockitoBean
    private CancelarCompra cancelarCompra;
    @MockitoBean
    private MarcarEnviada marcarEnviada;
    @MockitoBean
    private MarcarEntregada marcarEntregada;

    private static final String AGREGAR = """
            {"presentacionId": "pres-1", "cantidad": {"valor": 2, "unidad": "kg"}}""";
    private static final String DIRECCION = """
            {"ciudad": "Montenegro", "direccion": "Calle 10 # 5-20", "destinatario": "Carlos Perez"}""";

    /** Por defecto las peticiones llevan el token del Comprador dueño; un test puede poner otro con .with(token(...)). */
    @BeforeEach
    void prepararLaPeticion() {
        given(autorizacion.esDelComprador(any(), any())).willReturn(true);
        given(autorizacion.esVendedorDe(any(), any())).willReturn(true);
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .defaultRequest(get("/").with(token("comprador-1", "COMPRADOR")))
                .build();
    }

    private RequestPostProcessor token(String usuarioId, String rol) {
        return jwt().jwt(token -> token.subject(usuarioId).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    private Compra carritoConCafe() {
        Compra compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        return compra;
    }

    private Compra confirmada() {
        Compra compra = carritoConCafe();
        compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        return compra;
    }

    // ----- crear carrito -----

    @Test
    void crearCarritoResponde201ConLocationYElCompradorDelToken() throws Exception {
        given(crearCarrito.ejecutar(any(), eq("comprador-1"))).willReturn(Compra.abrirCarrito("compra-1", "comprador-1"));

        mockMvc.perform(post("/api/compras"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/compras/compra-1"))
                .andExpect(jsonPath("$.id").value("compra-1"))
                .andExpect(jsonPath("$.compradorId").value("comprador-1"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void unVendedorNoPuedeCrearCarritoYResponde403() throws Exception {
        mockMvc.perform(post("/api/compras").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isForbidden());

        verify(crearCarrito, never()).ejecutar(any(), any());
    }

    // ----- agregar al carrito -----

    @Test
    void agregarAlCarritoResponde200ConElPrecioCongelado() throws Exception {
        given(agregarAlCarrito.ejecutar(eq("compra-1"), eq("pres-1"), eq(new Cantidad(2, "kg"))))
                .willReturn(carritoConCafe());

        mockMvc.perform(post("/api/compras/compra-1/presentaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(AGREGAR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles[0].presentacionId").value("pres-1"))
                .andExpect(jsonPath("$.detalles[0].precioCongelado.monto").value(50000));
    }

    @Test
    void agregarSinPresentacionYConCantidadCeroResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(post("/api/compras/compra-1/presentaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"presentacionId": "", "cantidad": {"valor": 0, "unidad": "kg"}}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("La Presentacion a agregar es obligatoria")))
                .andExpect(jsonPath("$.mensaje").value(containsString("cantidad.valor")));

        verify(agregarAlCarrito, never()).ejecutar(any(), any(), any());
    }

    @Test
    void agregarSinStockSuficienteResponde400ConElMensajeDelDominio() throws Exception {
        given(agregarAlCarrito.ejecutar(any(), any(), any())).willThrow(new ReglaDominioException("Stock insuficiente"));

        mockMvc.perform(post("/api/compras/compra-1/presentaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(AGREGAR))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Stock insuficiente"));
    }

    @Test
    void agregarAUnaCompraQueNoExisteResponde404() throws Exception {
        given(agregarAlCarrito.ejecutar(any(), any(), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(post("/api/compras/no-existe/presentaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(AGREGAR))
                .andExpect(status().isNotFound());
    }

    @Test
    void agregarAlCarritoDeOtroCompradorResponde403SinLlamarAlCasoDeUso() throws Exception {
        given(autorizacion.esDelComprador(eq("compra-ajena"), any())).willReturn(false);

        mockMvc.perform(post("/api/compras/compra-ajena/presentaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(AGREGAR))
                .andExpect(status().isForbidden());

        verify(agregarAlCarrito, never()).ejecutar(any(), any(), any());
    }

    // ----- confirmar -----

    @Test
    void confirmarResponde200ConLaDireccionDeEnvio() throws Exception {
        given(confirmarCompra.ejecutar(eq("compra-1"), any())).willReturn(confirmada());

        mockMvc.perform(put("/api/compras/compra-1/confirmar")
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.direccionEnvio.ciudad").value("Montenegro"));
    }

    @Test
    void confirmarSinDireccionCompletaResponde400SinLlamarAlCasoDeUso() throws Exception {
        mockMvc.perform(put("/api/compras/compra-1/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ciudad": "Montenegro", "direccion": "", "destinatario": "Carlos Perez"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("La direccion es obligatoria")));

        verify(confirmarCompra, never()).ejecutar(any(), any());
    }

    @Test
    void confirmarUnCarritoVacioResponde400() throws Exception {
        given(confirmarCompra.ejecutar(any(), any()))
                .willThrow(new ReglaDominioException("No se puede confirmar una compra sin detalles"));

        mockMvc.perform(put("/api/compras/compra-1/confirmar")
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede confirmar una compra sin detalles"));
    }

    @Test
    void confirmarUnaCompraQueNoExisteResponde404() throws Exception {
        given(confirmarCompra.ejecutar(any(), any())).willThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/compras/no-existe/confirmar")
                        .contentType(MediaType.APPLICATION_JSON).content(DIRECCION))
                .andExpect(status().isNotFound());
    }

    // ----- cancelar -----

    @Test
    void cancelarResponde200() throws Exception {
        Compra cancelada = carritoConCafe();
        cancelada.cancelar();
        given(cancelarCompra.ejecutar("compra-1")).willReturn(cancelada);

        mockMvc.perform(put("/api/compras/compra-1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void cancelarUnaCompraEnviadaResponde400() throws Exception {
        given(cancelarCompra.ejecutar("compra-1"))
                .willThrow(new ReglaDominioException("No se puede cancelar una compra en estado ENVIADA"));

        mockMvc.perform(put("/api/compras/compra-1/cancelar"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No se puede cancelar una compra en estado ENVIADA"));
    }

    @Test
    void cancelarUnaCompraQueNoExisteResponde404() throws Exception {
        given(cancelarCompra.ejecutar("no-existe")).willThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/compras/no-existe/cancelar")).andExpect(status().isNotFound());
    }

    // ----- enviar y entregar (vendedor) -----

    @Test
    void elVendedorMarcaEnviadaYResponde200() throws Exception {
        Compra enviada = confirmada();
        enviada.enviar();
        given(marcarEnviada.ejecutar("compra-1")).willReturn(enviada);

        mockMvc.perform(put("/api/compras/compra-1/enviar").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADA"));
    }

    @Test
    void elCompradorNoPuedeMarcarEnviadaYResponde403() throws Exception {
        mockMvc.perform(put("/api/compras/compra-1/enviar"))
                .andExpect(status().isForbidden());

        verify(marcarEnviada, never()).ejecutar(any());
    }

    @Test
    void enviarUnaCompraQueNoExisteResponde404() throws Exception {
        given(marcarEnviada.ejecutar("no-existe")).willThrow(new NoSuchElementException());

        mockMvc.perform(put("/api/compras/no-existe/enviar").with(token("vendedor-1", "CAFICULTOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void elVendedorMarcaEntregadaYResponde200ConLaFecha() throws Exception {
        Compra entregada = confirmada();
        entregada.enviar();
        entregada.entregar(LocalDateTime.of(2026, 10, 20, 15, 30));
        given(marcarEntregada.ejecutar("compra-1")).willReturn(entregada);

        mockMvc.perform(put("/api/compras/compra-1/entregar").with(token("vendedor-1", "VENDEDOR_DERIVADOS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADA"))
                .andExpect(jsonPath("$.fechaEntrega").exists());
    }

    @Test
    void entregarUnaCompraNoEnviadaResponde400() throws Exception {
        given(marcarEntregada.ejecutar("compra-1"))
                .willThrow(new ReglaDominioException("Solo se puede entregar una compra enviada (estado actual: CONFIRMADA)"));

        mockMvc.perform(put("/api/compras/compra-1/entregar").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("Solo se puede entregar una compra enviada")));
    }
}
