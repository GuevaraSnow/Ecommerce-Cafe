package com.uniquindio.ecommerce.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Seguridad de la API: valida el token JWT (firmado con HS256) de cada petición y deja el id del
 * usuario y su Rol en el contexto de seguridad. Quién puede hacer qué se declara con
 * {@code @PreAuthorize} en cada controlador, por eso aquí solo hay reglas generales:
 * <ul>
 *   <li>consultar Presentaciones (GET) es público;</li>
 *   <li>cualquier otra operación sobre Presentaciones exige token (sin token, 401; con token
 *       pero sin permiso, 403);</li>
 *   <li>el resto de rutas queda abierto hasta que su dueño las proteja con {@code @PreAuthorize}
 *       y las agregue aquí.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtroDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // la consola H2 se muestra dentro de frames de la misma página
                .headers(cabeceras -> cabeceras.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.GET, "/api/presentaciones/**").permitAll()
                        .requestMatchers("/api/presentaciones/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorDeAutoridades())));
        return http.build();
    }

    @Bean
    JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secreto) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(secreto)));
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secreto) {
        return NimbusJwtDecoder.withSecretKey(clave(secreto)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** El Rol del token (claim "rol") se vuelve una autoridad con su mismo nombre, sin prefijo: CAFICULTOR, TOSTADOR... */
    private JwtAuthenticationConverter convertidorDeAutoridades() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName(JwtService.CLAIM_ROL);
        autoridades.setAuthorityPrefix("");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(autoridades);
        return convertidor;
    }

    private SecretKey clave(String secreto) {
        return new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
