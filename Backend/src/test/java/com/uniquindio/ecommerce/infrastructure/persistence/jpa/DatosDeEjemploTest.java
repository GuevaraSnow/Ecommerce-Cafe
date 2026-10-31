package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los datos de ejemplo (data-*.sql) sirven para usar la aplicación: todos los usuarios inician sesión con
 * la contraseña Cafe2026* y ningún dato apunta a un id que no existe.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DatosDeEjemploTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @ParameterizedTest
    @CsvSource({
            "carlos@finca.com,usr-caficultor,CAFICULTOR",
            "teresa@tueste.com,usr-tostador,TOSTADOR",
            "valeria@derivados.com,usr-derivados,VENDEDOR_DERIVADOS",
            "ana@correo.com,usr-ana,COMPRADOR",
            "bruno@correo.com,usr-bruno,COMPRADOR",
            "carla@correo.com,usr-carla,COMPRADOR",
            "diego@correo.com,usr-diego,COMPRADOR"
    })
    void cadaUsuarioDeEjemploIniciaSesionConLaContrasenaDeEjemplo(String email, String id, String rol) throws Exception {
        mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"contrasena\": \"Cafe2026*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.id").value(id))
                .andExpect(jsonPath("$.usuario.rol").value(rol));
    }

    @Test
    void losUsuariosDeEjemploSonLosQueUsanLosDemasDatos() {
        assertEquals(0, huerfanos("presentaciones", "vendedor_id", "tbl_usuarios", "id"));
        assertEquals(0, huerfanos("compras", "comprador_id", "tbl_usuarios", "id"));
        assertEquals(0, huerfanos("resenas", "comprador_id", "tbl_usuarios", "id"));
        assertEquals(0, huerfanos("resenas", "vendedor_id", "tbl_usuarios", "id"));
        assertEquals(0, huerfanos("solicitudes_reembolso", "vendedor_id", "tbl_usuarios", "id"));
        assertEquals(0, huerfanos("tbl_fincas", "propietario_id", "tbl_usuarios", "id"));
        assertEquals(0, huerfanos("tbl_transformaciones", "tostador_id", "tbl_usuarios", "id"));
    }

    @Test
    void laProduccionDeEjemploEncadenaFincaLoteTransformacionYPresentacion() {
        assertEquals(0, huerfanos("tbl_lotes", "finca_id", "tbl_fincas", "id"));
        assertEquals(0, huerfanos("tbl_transformaciones", "lote_codigo", "tbl_lotes", "codigo"));
        assertEquals(0, huerfanos("presentaciones", "lote_id", "tbl_lotes", "codigo"));
        assertEquals(0, huerfanos("presentaciones", "transformacion_id", "tbl_transformaciones", "id"));
        assertEquals(0, huerfanos("detalles_compra", "presentacion_id", "presentaciones", "id"));
    }

    @Test
    void cadaPresentacionTieneComoDuenoAUnVendedorConElRolQueLaPublica() {
        // El café verde lo publica el Caficultor, el tostado el Tostador y el merchandising el Vendedor de Derivados.
        Integer conOtroRol = jdbc.queryForObject("""
                select count(*) from presentaciones p join tbl_usuarios u on u.id = p.vendedor_id
                where (p.tipo = 'CAFE_VERDE' and u.rol <> 'CAFICULTOR')
                   or (p.tipo = 'CAFE_TOSTADO' and u.rol <> 'TOSTADOR')
                   or (p.tipo = 'MERCHANDISING' and u.rol <> 'VENDEDOR_DERIVADOS')""", Integer.class);

        assertEquals(0, conOtroRol);
    }

    /** Cuántas filas de {@code tabla} tienen en {@code columna} un valor que no existe en {@code destino.clave}. */
    private int huerfanos(String tabla, String columna, String destino, String clave) {
        return jdbc.queryForObject("select count(*) from " + tabla + " t where t." + columna + " is not null"
                + " and t." + columna + " not in (select " + clave + " from " + destino + ")", Integer.class);
    }
}
