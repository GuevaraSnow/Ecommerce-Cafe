package com.uniquindio.ecommerce.infrastructure.rest.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Escribe directamente en la respuesta el mismo cuerpo JSON que {@link ErrorResponse}, para los errores que
 * salen del filtro de seguridad (401 y 403) y no pasan por {@link GlobalExceptionHandler}: así toda respuesta
 * de error de la API tiene el mismo formato.
 */
public final class EscritorDeErrores {

    private EscritorDeErrores() {
    }

    public static void escribir(HttpServletResponse respuesta, HttpStatus status, String mensaje) throws IOException {
        respuesta.setStatus(status.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        respuesta.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":" + status.value()
                + ",\"mensaje\":\"" + escapar(mensaje) + "\"}");
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
