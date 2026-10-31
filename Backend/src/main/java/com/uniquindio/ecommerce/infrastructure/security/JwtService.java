package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Emite los tokens JWT de la API. El token lleva el id del usuario como sujeto (los {@code @PreAuthorize}
 * comparan con él), su correo en el claim {@value #CLAIM_CORREO}, su Rol en el claim {@value #CLAIM_ROL} y
 * el vencimiento ({@code exp}); {@link SecurityConfig} los lee de vuelta al validar cada petición.
 * Lo usa el inicio de sesión para devolver el token.
 */
@Component
public class JwtService {

    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_CORREO = "correo";

    private final JwtEncoder encoder;
    private final Duration duracion;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.duracion-minutos}") long duracionEnMinutos) {
        this.encoder = encoder;
        this.duracion = Duration.ofMinutes(duracionEnMinutos);
    }

    public String generarToken(String usuarioId, String correo, Rol rol) {
        return generarToken(usuarioId, correo, rol, Instant.now());
    }

    /** Variante con el instante de emisión explícito, para poder probar tokens vencidos. */
    String generarToken(String usuarioId, String correo, Rol rol, Instant emitidoEn) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuarioId)
                .claim(CLAIM_CORREO, correo)
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(emitidoEn)
                .expiresAt(emitidoEn.plus(duracion))
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
    }
}
