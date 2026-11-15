package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.AlmacenDeImagenes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;

/**
 * Adaptador de AlmacenDeImagenes que sube las imágenes a Cloudinary con una subida firmada.
 * Las credenciales salen de variables de entorno: nunca van en el repositorio.
 */
@Component
public class AlmacenCloudinary implements AlmacenDeImagenes {

    private static final Logger log = LoggerFactory.getLogger(AlmacenCloudinary.class);
    private static final String MENSAJE_NO_DISPONIBLE = "No se pudo guardar la imagen, intenta más tarde";
    private static final String CARPETA = "cafe-de-origen";

    private final RestClient cliente;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public AlmacenCloudinary(@Value("${app.cloudinary.cloud-name}") String cloudName,
                             @Value("${app.cloudinary.api-key}") String apiKey,
                             @Value("${app.cloudinary.api-secret}") String apiSecret) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(30));
        this.cliente = RestClient.builder().requestFactory(fabrica).build();
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public String guardar(byte[] contenido, String nombreOriginal, String tipo) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ReglaDominioException(MENSAJE_NO_DISPONIBLE);
        }

        // Cloudinary firma los parámetros en orden alfabético, unidos con &, seguidos del secreto
        long marcaDeTiempo = System.currentTimeMillis() / 1000;
        String firma = sha1("folder=" + CARPETA + "&timestamp=" + marcaDeTiempo + apiSecret);

        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("file", new ByteArrayResource(contenido) {
            @Override
            public String getFilename() {
                return nombreOriginal == null || nombreOriginal.isBlank() ? "imagen" : nombreOriginal;
            }
        });
        cuerpo.add("api_key", apiKey);
        cuerpo.add("timestamp", String.valueOf(marcaDeTiempo));
        cuerpo.add("folder", CARPETA);
        cuerpo.add("signature", firma);

        try {
            Map<?, ?> respuesta = cliente.post()
                    .uri("https://api.cloudinary.com/v1_1/{nube}/image/upload", cloudName)
                    .body(cuerpo)
                    .retrieve()
                    .body(Map.class);
            return (String) respuesta.get("secure_url");
        } catch (RestClientException | ClassCastException | NullPointerException e) {
            // El detalle real queda en el log del servidor (nunca las credenciales)
            log.warn("Falló la subida a Cloudinary: {}", e.toString());
            throw new ReglaDominioException(MENSAJE_NO_DISPONIBLE);
        }
    }

    private String sha1(String texto) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-1").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 no está disponible", e);
        }
    }
}
