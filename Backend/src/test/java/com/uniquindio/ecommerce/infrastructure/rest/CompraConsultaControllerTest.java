package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.application.usecase.ConsultarCompra;
import com.uniquindio.ecommerce.application.usecase.ConsultarHistorialDeCompras;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.CompraMapper;
import com.uniquindio.ecommerce.infrastructure.security.AutorizacionDeCompra;
import com.uniquindio.ecommerce.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Consultas de CompraConsultaController: el historial es siempre el del Comprador del token y el detalle
 * solo lo ven su Comprador o un vendedor de la Compra. La regla de propiedad se prueba en AutorizacionDeCompraTest.
 */
@WebMvcTest(CompraConsultaController.class)
@Import({CompraMapper.class, SecurityConfig.class})
class CompraConsultaControllerTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @MockitoBean(name = "autorizacionDeCompra")
    private AutorizacionDeCompra autorizacion;

    @MockitoBean
    private ConsultarHistorialDeCompras consultarHistorial;
    @MockitoBean
    private ConsultarCompra consultarCompra;

    /** Por defecto las peticiones llevan el token del Comprador dueño; un test puede poner otro con .with(token(...)). */
    @BeforeEach
    void prepararLaPeticion() {
        given(autorizacion.esDelComprador(any(), any())).willReturn(true);
        given(autorizacion.esVendedorDe(any(), any())).willReturn(false);
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto)
                .apply(springSecurity())
                .defaultRequest(get("/").with(token("comprador-1", "COMPRADOR")))
                .build();
    }

    private RequestPostProcessor token(String usuarioId, String rol) {
        return jwt().jwt(token -> token.subject(usuarioId).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    private Compra compraCon(String compraId, String presentacionId) {
        Compra compra = Compra.abrirCarrito(compraId, "comprador-1");
        compra.agregarPresentacion(presentacionId, new Precio(50000, "COP"), new Cantidad(2, "kg"));
        return compra;
    }

    // ----- historial -----

    @Test
    void historialResponde200ConLasComprasDelCompradorDelToken() throws Exception {
        given(consultarHistorial.ejecutar("comprador-1"))
                .willReturn(List.of(compraCon("compra-2", "pres-2"), compraCon("compra-1", "pres-1")));

        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("compra-2"))
                .andExpect(jsonPath("$[1].id").value("compra-1"))
                .andExpect(jsonPath("$[0].detalles[0].presentacionId").value("pres-2"));
    }

    @Test
    void historialSinComprasResponde200ConUnaListaVacia() throws Exception {
        given(consultarHistorial.ejecutar("comprador-1")).willReturn(List.of());

        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unVendedorNoTieneHistorialDeComprasYResponde403() throws Exception {
        mockMvc.perform(get("/api/compras").with(token("vendedor-1", "TOSTADOR")))
                .andExpect(status().isForbidden());

        verify(consultarHistorial, never()).ejecutar(any());
    }

    @Test
    void historialSinTokenResponde401() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();

        mockMvc.perform(get("/api/compras")).andExpect(status().isUnauthorized());

        verify(consultarHistorial, never()).ejecutar(any());
    }

    // ----- detalle -----

    @Test
    void elCompradorVeElDetalleDeSuCompra() throws Exception {
        given(consultarCompra.ejecutar("compra-1")).willReturn(compraCon("compra-1", "pres-1"));

        mockMvc.perform(get("/api/compras/compra-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("compra-1"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.detalles[0].precioCongelado.monto").value(50000));
    }

    @Test
    void unVendedorDeLaCompraVeSuDetalle() throws Exception {
        given(autorizacion.esDelComprador(eq("compra-1"), any())).willReturn(false);
        given(autorizacion.esVendedorDe(eq("compra-1"), any())).willReturn(true);
        given(consultarCompra.ejecutar("compra-1")).willReturn(compraCon("compra-1", "pres-1"));

        mockMvc.perform(get("/api/compras/compra-1").with(token("vendedor-1", "CAFICULTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("compra-1"));
    }

    @Test
    void quienNoEsDuenoNiVendedorDeLaCompraRecibe403() throws Exception {
        given(autorizacion.esDelComprador(eq("compra-ajena"), any())).willReturn(false);

        mockMvc.perform(get("/api/compras/compra-ajena"))
                .andExpect(status().isForbidden());

        verify(consultarCompra, never()).ejecutar(any());
    }

    @Test
    void elDetalleDeUnaCompraQueNoExisteResponde404() throws Exception {
        given(consultarCompra.ejecutar("no-existe")).willThrow(new NoSuchElementException());

        mockMvc.perform(get("/api/compras/no-existe"))
                .andExpect(status().isNotFound());
    }
}
