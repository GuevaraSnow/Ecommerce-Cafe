package com.uniquindio.ecommerce.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la seguridad real de la API (con el filtro de seguridad completo, no un @WebMvcTest):
 * sin token responde 401; con token pero sin permiso, 403; con el rol y el dueño correctos, 200 o 201.
 * Los usuarios del token se simulan con {@code jwt()}; los tokens reales los emite {@link JwtService}.
 * El dueño de pres-taza-1 (datos de ejemplo) es usr-derivados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeguridadPresentacionTest {

    private static final String ARTICULO = """
            {"titulo": "Posavasos", "descripcion": "Posavasos de corcho", "material": "corcho",
             "precio": {"monto": 9000, "moneda": "COP"}, "cantidad": {"valor": 30, "unidad": "unidad"},
             "galeria": [{"url": "http://img/p.png", "principal": true}]}""";

    private static final String NUEVO_PRECIO = """
            {"precio": {"monto": 20000, "moneda": "COP"}}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private RequestPostProcessor usuario(String id, String rol) {
        return jwt().jwt(token -> token.subject(id).claim("rol", rol))
                .authorities(new SimpleGrantedAuthority(rol));
    }

    // ----- 401: sin token -----

    @Test
    void publicarSinTokenResponde401() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void modificarYDarDeBajaSinTokenResponden401() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-taza-1/precio")
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRECIO))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/presentaciones/pres-taza-1")).andExpect(status().isUnauthorized());
    }

    @Test
    void consultarElCatalogoNoNecesitaToken() throws Exception {
        mockMvc.perform(get("/api/presentaciones")).andExpect(status().isOk());
        mockMvc.perform(get("/api/presentaciones/pres-taza-1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/presentaciones/sugerencias").param("texto", "taza")).andExpect(status().isOk());
    }

    // ----- 403: con token pero sin permiso -----

    @Test
    void publicarConElRolDeCompradorResponde403() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .with(usuario("usr-ana", "COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isForbidden());
    }

    @Test
    void otroVendedorNoPuedeCambiarElPrecioDeUnaPresentacionAjena() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-taza-1/precio")
                        .with(usuario("usr-tostador", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRECIO))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/presentaciones/pres-taza-1"))
                .andExpect(jsonPath("$.precio.monto").value(15000));
    }

    @Test
    void otroVendedorNoPuedeDarDeBajaNiPausarUnaPresentacionAjena() throws Exception {
        mockMvc.perform(delete("/api/presentaciones/pres-taza-1").with(usuario("usr-tostador", "TOSTADOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/presentaciones/pres-taza-1/pausa").with(usuario("usr-tostador", "TOSTADOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/presentaciones/pres-taza-1"))
                .andExpect(jsonPath("$.eliminada").value(false))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void ningunaOperacionDeModificacionDejaPasarAUnComprador() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-taza-1/precio")
                        .with(usuario("usr-ana", "COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRECIO))
                .andExpect(status().isForbidden());
    }

    // ----- 200 y 201: rol y dueño correctos -----

    @Test
    void elDuenoPuedeCambiarElPrecioDeSuPresentacion() throws Exception {
        mockMvc.perform(put("/api/presentaciones/pres-taza-1/precio")
                        .with(usuario("usr-derivados", "VENDEDOR_DERIVADOS"))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRECIO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio.monto").value(20000));
    }

    @Test
    void elDuenoPuedeDarDeBajaSuPresentacion() throws Exception {
        mockMvc.perform(delete("/api/presentaciones/pres-taza-1").with(usuario("usr-derivados", "VENDEDOR_DERIVADOS")))
                .andExpect(status().isNoContent());
    }

    @Test
    void elVendedorYSuRolSalenDelTokenNoDelCuerpo() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .with(usuario("usr-nuevo", "VENDEDOR_DERIVADOS"))
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendedorId").value("usr-nuevo"));
    }

    @Test
    void elRolDelTokenDecideQueSePuedePublicar() throws Exception {
        // un Tostador pasa la barrera de roles, pero el dominio no le deja publicar Merchandising
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .with(usuario("usr-tostador", "TOSTADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unaPresentacionInexistenteResponde404AunqueElUsuarioNoSeaSuDueno() throws Exception {
        mockMvc.perform(put("/api/presentaciones/no-existe/precio")
                        .with(usuario("usr-derivados", "VENDEDOR_DERIVADOS"))
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRECIO))
                .andExpect(status().isNotFound());
    }

    // ----- tokens reales -----

    @Test
    void unTokenRealEmitidoPorElServicioPermitePublicar() throws Exception {
        String token = jwtService.generarToken("usr-derivados", Rol.VENDEDOR_DERIVADOS);

        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendedorId").value("usr-derivados"));
    }

    @Test
    void unTokenRealDeOtroVendedorNoPuedeModificarUnaPresentacionAjena() throws Exception {
        String token = jwtService.generarToken("usr-tostador", Rol.TOSTADOR);

        mockMvc.perform(put("/api/presentaciones/pres-taza-1/precio")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(NUEVO_PRECIO))
                .andExpect(status().isForbidden());
    }

    @Test
    void unTokenVencidoResponde401() throws Exception {
        String vencido = jwtService.generarToken("usr-derivados", Rol.VENDEDOR_DERIVADOS,
                Instant.now().minusSeconds(2 * 60 * 60));

        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + vencido)
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenFirmadoConOtraClaveResponde401() throws Exception {
        NimbusJwtEncoder otroEmisor = new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(
                "otra-clave-distinta-con-mas-de-32-caracteres".getBytes(StandardCharsets.UTF_8), "HmacSHA256")));
        JwtClaimsSet claims = JwtClaimsSet.builder().subject("usr-derivados").claim("rol", "VENDEDOR_DERIVADOS")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600)).build();
        String falsificado = otroEmisor.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + falsificado)
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unTokenQueNoEsUnJwtResponde401() throws Exception {
        mockMvc.perform(post("/api/presentaciones/merchandising")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer esto-no-es-un-token")
                        .contentType(MediaType.APPLICATION_JSON).content(ARTICULO))
                .andExpect(status().isUnauthorized());
    }
}
