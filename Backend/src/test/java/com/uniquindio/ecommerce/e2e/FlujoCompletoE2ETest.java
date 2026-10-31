package com.uniquindio.ecommerce.e2e;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba end-to-end: la aplicación completa (seguridad JWT real, controladores, casos de uso, dominio y H2),
 * recorrida solo por HTTP, como lo haría el front. Nada es simulado: los tokens salen del login.
 *
 * <p>Flujo: registrar vendedor y comprador, iniciar sesión, publicar una Presentación, crear el carrito,
 * agregar, confirmar (descuenta stock), enviar, entregar, dejar y responder una reseña, pedir el reembolso
 * y aprobarlo (la Compra queda REEMBOLSADA).
 *
 * <p>La reseña va antes de aprobar el reembolso: reseñar exige una Compra ENTREGADA, y al aprobar el
 * reembolso de todas sus Presentaciones la Compra pasa a REEMBOLSADA.
 *
 * <p>@Transactional deshace todo al terminar, así no ensucia los datos de las demás pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FlujoCompletoE2ETest {

    private static final String CONTRASENA = "CafeTrazado2026";

    @Autowired
    private MockMvc mockMvc;

    // ----- ayudas: cada una es una petición HTTP real -----

    private void registrar(String nombre, String email, String rol) throws Exception {
        mockMvc.perform(post("/api/usuarios/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "%s", "email": "%s", "contrasena": "%s",
                                 "telefono": "3001234567", "fechaNacimiento": "1998-04-15", "rol": "%s"}"""
                                .formatted(nombre, email, CONTRASENA, rol)))
                .andExpect(status().isCreated());
    }

    /** Inicia sesión y devuelve el token JWT de la respuesta. */
    private String iniciarSesion(String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/usuarios/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "contrasena": "%s"}""".formatted(email, CONTRASENA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        return leer(resultado, "$.token");
    }

    /** Agrega el token a la petición, como lo hace el front: Authorization: Bearer ... */
    private MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder peticion, String token) {
        return peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private String leer(MvcResult resultado, String ruta) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), ruta);
    }

    // ----- el flujo -----

    @Test
    void unaCompraRecorreTodoSuCicloDeVidaDesdeElRegistroHastaElReembolso() throws Exception {
        // 1. Registro e inicio de sesión de los dos actores
        registrar("Valentina Derivados", "valentina.e2e@cafetrazado.com", "VENDEDOR_DERIVADOS");
        registrar("Andres Comprador", "andres.e2e@cafetrazado.com", "COMPRADOR");
        String vendedor = iniciarSesion("valentina.e2e@cafetrazado.com");
        String comprador = iniciarSesion("andres.e2e@cafetrazado.com");

        // 2. El vendedor publica una Presentación con 20 unidades
        MvcResult publicada = mockMvc.perform(conToken(post("/api/presentaciones/merchandising"), vendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Taza de ceramica", "descripcion": "Taza artesanal de 300 ml",
                                 "material": "ceramica", "precio": {"monto": 25000, "moneda": "COP"},
                                 "cantidad": {"valor": 20, "unidad": "unidad"},
                                 "galeria": [{"url": "http://img/taza.png", "principal": true}]}"""))
                .andExpect(status().isCreated())
                .andReturn();
        String presentacionId = leer(publicada, "$.id");

        // 3. El comprador abre su carrito; el comprador sale del token
        MvcResult carrito = mockMvc.perform(conToken(post("/api/compras"), comprador))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn();
        String compraId = leer(carrito, "$.id");

        // 4. Agrega 2 unidades: el precio queda congelado, el stock todavía no se toca
        mockMvc.perform(conToken(post("/api/compras/{id}/presentaciones", compraId), comprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"presentacionId": "%s", "cantidad": {"valor": 2, "unidad": "unidad"}}"""
                                .formatted(presentacionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles[0].precioCongelado.monto").value(25000));
        mockMvc.perform(get("/api/presentaciones/{id}", presentacionId))
                .andExpect(jsonPath("$.cantidadDisponible.valor").value(20));

        // 5. Confirma con la dirección de envío: ahora sí se descuenta el stock
        mockMvc.perform(conToken(put("/api/compras/{id}/confirmar", compraId), comprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ciudad": "Montenegro", "direccion": "Calle 10 # 5-20", "destinatario": "Andres Comprador"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
        mockMvc.perform(get("/api/presentaciones/{id}", presentacionId))
                .andExpect(jsonPath("$.cantidadDisponible.valor").value(18));

        // 6. El vendedor la envía y la entrega
        mockMvc.perform(conToken(put("/api/compras/{id}/enviar", compraId), vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADA"));
        mockMvc.perform(conToken(put("/api/compras/{id}/entregar", compraId), vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADA"))
                .andExpect(jsonPath("$.fechaEntrega").exists());

        // 7. Con la Compra entregada, el comprador deja una reseña y el vendedor la responde
        MvcResult resena = mockMvc.perform(conToken(post("/api/resenas"), comprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"presentacionId": "%s", "calificacion": 4, "texto": "Bonita, llego bien empacada"}"""
                                .formatted(presentacionId)))
                .andExpect(status().isCreated())
                .andReturn();
        String resenaId = leer(resena, "$.id");

        mockMvc.perform(conToken(put("/api/resenas/{id}/respuesta", resenaId), vendedor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"texto": "Gracias por tu compra"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta.texto").value("Gracias por tu compra"));

        // Las reseñas son públicas: se consultan sin token
        mockMvc.perform(get("/api/resenas").param("presentacionId", presentacionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].calificacion").value(4));

        // 8. Dentro de las 48 h, el comprador pide el reembolso y el vendedor lo aprueba
        MvcResult solicitud = mockMvc.perform(conToken(post("/api/reembolsos"), comprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"compraId": "%s", "presentacionId": "%s", "motivo": "La taza llego con una grieta"}"""
                                .formatted(compraId, presentacionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("SOLICITADO"))
                .andReturn();
        String solicitudId = leer(solicitud, "$.id");

        mockMvc.perform(conToken(put("/api/reembolsos/{id}/aprobar", solicitudId), vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));

        // 9. Era su única Presentación, así que la Compra queda REEMBOLSADA (y aparece en el historial)
        mockMvc.perform(conToken(get("/api/compras/{id}", compraId), comprador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REEMBOLSADA"));
        mockMvc.perform(conToken(get("/api/compras"), comprador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(compraId));
    }

    @Test
    void losErroresDelFlujoRespondenConElCodigoCorrecto() throws Exception {
        registrar("Andres Comprador", "andres.errores@cafetrazado.com", "COMPRADOR");
        String comprador = iniciarSesion("andres.errores@cafetrazado.com");
        MvcResult carrito = mockMvc.perform(conToken(post("/api/compras"), comprador))
                .andExpect(status().isCreated())
                .andReturn();
        String compraId = leer(carrito, "$.id");

        // Sin token: 401
        mockMvc.perform(post("/api/compras")).andExpect(status().isUnauthorized());

        // Confirmar un carrito vacío rompe una regla del dominio: 422
        mockMvc.perform(conToken(put("/api/compras/{id}/confirmar", compraId), comprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ciudad": "Armenia", "direccion": "Cra 14 # 10-20", "destinatario": "Andres"}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("No se puede confirmar una compra sin detalles"));

        // Agregar una Presentación que no existe: 404
        mockMvc.perform(conToken(post("/api/compras/{id}/presentaciones", compraId), comprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"presentacionId": "pres-no-existe", "cantidad": {"valor": 1, "unidad": "unidad"}}"""))
                .andExpect(status().isNotFound());

        // Reseñar sin haber recibido la Presentación: 422
        mockMvc.perform(conToken(post("/api/resenas"), comprador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"presentacionId": "pres-taza-1", "calificacion": 5, "texto": "Nunca la compre"}"""))
                .andExpect(status().isUnprocessableEntity());

        // Un comprador no puede marcar una compra como enviada: 403
        mockMvc.perform(conToken(put("/api/compras/{id}/enviar", compraId), comprador))
                .andExpect(status().isForbidden());
    }
}
