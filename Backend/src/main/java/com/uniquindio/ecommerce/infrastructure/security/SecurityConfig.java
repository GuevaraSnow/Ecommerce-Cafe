package com.uniquindio.ecommerce.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.uniquindio.ecommerce.infrastructure.rest.exception.EscritorDeErrores;
import com.uniquindio.ecommerce.infrastructure.rest.exception.GlobalExceptionHandler;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

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
                .headers(cabeceras -> cabeceras.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(rutas -> rutas

                        .requestMatchers("/api/usuarios/registro", "/api/usuarios/login",
                                "/api/usuarios/recuperar-contrasena", "/api/usuarios/restablecer-contrasena").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html", "/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/presentaciones/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resenas").permitAll()
                        // El contenedor reenvía a /error al terminar una petición con error; si estuviera protegido, el
                        // 404 o el 500 reales quedarían tapados por un 401.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(puntoDeEntrada())
                        .accessDeniedHandler(accesoDenegado()))
                .oauth2ResourceServer(servidor -> servidor
                        .authenticationEntryPoint(puntoDeEntrada())
                        .accessDeniedHandler(accesoDenegado())
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

    /** 401: sin token, token inválido o vencido. Responde el mismo cuerpo JSON que el resto de errores de la API. */
    private AuthenticationEntryPoint puntoDeEntrada() {
        return (peticion, respuesta, ex) -> {
            respuesta.setHeader("WWW-Authenticate", "Bearer");
            EscritorDeErrores.escribir(respuesta, HttpStatus.UNAUTHORIZED, GlobalExceptionHandler.MENSAJE_AUTENTICACION_REQUERIDA);
        };
    }

    /** 403: autenticado pero sin permiso. Mismo cuerpo JSON que el resto de errores de la API. */
    private AccessDeniedHandler accesoDenegado() {
        return (peticion, respuesta, ex) ->
                EscritorDeErrores.escribir(respuesta, HttpStatus.FORBIDDEN, GlobalExceptionHandler.MENSAJE_SIN_PERMISO);
    }

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