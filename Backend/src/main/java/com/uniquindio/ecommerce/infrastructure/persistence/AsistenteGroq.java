package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.AsistenteIA;
import com.uniquindio.ecommerce.Domain.valueobject.MensajeDeChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adaptador de AsistenteIA que consulta la API de Groq (compatible con OpenAI).
 * La clave y el modelo salen de variables de entorno: nunca van en el repositorio.
 */
@Component
public class AsistenteGroq implements AsistenteIA {

    private static final Logger log = LoggerFactory.getLogger(AsistenteGroq.class);
    private static final String MENSAJE_NO_DISPONIBLE = "El asesor no está disponible en este momento, intenta más tarde";

    private final RestClient cliente;
    private final String apiKey;
    private final String modelo;

    public AsistenteGroq(@Value("${app.groq.url}") String url,
                         @Value("${app.groq.api-key}") String apiKey,
                         @Value("${app.groq.model}") String modelo) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(20));
        this.cliente = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
        this.apiKey = apiKey;
        this.modelo = modelo;
    }

    @Override
    public String responder(String instrucciones, List<MensajeDeChat> historial, String mensaje) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ReglaDominioException(MENSAJE_NO_DISPONIBLE);
        }

        try {
            Map<?, ?> respuesta = cliente.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .body(Map.of(
                            "model", modelo,
                            "temperature", 0.4,
                            "reasoning_effort", "low",
                            "max_tokens", 1000,
                            "messages", mensajes(instrucciones, historial, mensaje)))
                    .retrieve()
                    .body(Map.class);
            return extraerTexto(respuesta);
        } catch (RestClientException | ClassCastException | NullPointerException e) {
            // Sin red, clave inválida, límite de Groq o respuesta rara: el dominio solo ve "no disponible"
            // y el detalle real queda en el log del servidor (nunca la clave)
            log.warn("Falló la consulta a Groq: {}", e.toString());
            throw new ReglaDominioException(MENSAJE_NO_DISPONIBLE);
        }
    }

    // Traduce los roles del dominio (usuario/asistente) a los de Groq (user/assistant)
    private List<Map<String, String>> mensajes(String instrucciones, List<MensajeDeChat> historial, String mensaje) {
        List<Map<String, String>> mensajes = new ArrayList<>();
        mensajes.add(Map.of("role", "system", "content", instrucciones));
        for (MensajeDeChat anterior : historial) {
            String rol = anterior.rol().equals("usuario") ? "user" : "assistant";
            mensajes.add(Map.of("role", rol, "content", anterior.texto()));
        }
        mensajes.add(Map.of("role", "user", "content", mensaje));
        return mensajes;
    }

    // La respuesta de Groq trae: choices[0].message.content
    private String extraerTexto(Map<?, ?> respuesta) {
        List<?> opciones = (List<?>) respuesta.get("choices");
        Map<?, ?> primera = (Map<?, ?>) opciones.get(0);
        Map<?, ?> contenido = (Map<?, ?>) primera.get("message");
        return ((String) contenido.get("content")).trim();
    }
}
