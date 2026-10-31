package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.application.dto.request.DejarResenaRequest;
import com.uniquindio.ecommerce.application.dto.request.ResponderResenaRequest;
import com.uniquindio.ecommerce.application.dto.response.ResenaResponse;
import com.uniquindio.ecommerce.application.usecase.DejarResena;
import com.uniquindio.ecommerce.application.usecase.ResponderResena;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.ResenaMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Operaciones que crean o modifican una Reseña: un endpoint por operación del dominio.
 *
 * <p>Quién actúa sale del token. Que el Comprador haya recibido la Presentación, que no la haya reseñado antes,
 * y que solo el vendedor dueño responda una sola vez, lo valida el dominio (responde 422 si no se cumple).
 */
@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Dejar y responder reseñas de Presentaciones")
@SecurityRequirement(name = "bearerAuth")
public class ResenaController {

    private static final String COMPRADOR = "hasAuthority('COMPRADOR')";
    private static final String VENDEDOR = "hasAnyAuthority('CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS')";

    private final DejarResena dejarResena;
    private final ResponderResena responderResena;
    private final ResenaMapper mapper;

    @PostMapping
    @PreAuthorize(COMPRADOR)
    @Operation(summary = "Dejar una reseña",
            description = "Calificación de 1 a 5 y comentario; solo si el Comprador recibió la Presentación y una sola vez.")
    @ApiResponse(responseCode = "201", description = "Reseña creada; el Location apunta a ella")
    @ApiResponse(responseCode = "400", description = "Datos mal formados")
    @ApiResponse(responseCode = "422", description = "Sin compra entregada o ya reseñada")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public ResponseEntity<ResenaResponse> dejar(@AuthenticationPrincipal Jwt token,
                                                @Valid @RequestBody DejarResenaRequest request) {
        Resena resena = dejarResena.ejecutar(UUID.randomUUID().toString(), token.getSubject(),
                request.presentacionId(), mapper.toCalificacion(request.calificacion()), mapper.toTexto(request.texto()));
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/resenas/{id}").buildAndExpand(resena.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toResponse(resena));
    }

    @PutMapping("/{id}/respuesta")
    @PreAuthorize(VENDEDOR)
    @Operation(summary = "Responder una reseña", description = "Solo el vendedor dueño de la Presentación, una sola vez.")
    @ApiResponse(responseCode = "422", description = "Respuesta vacía, no es el vendedor dueño o ya se respondió")
    @ApiResponse(responseCode = "404", description = "No existe la reseña")
    public ResenaResponse responder(@PathVariable String id, @AuthenticationPrincipal Jwt token,
                                    @Valid @RequestBody ResponderResenaRequest request) {
        return mapper.toResponse(responderResena.ejecutar(id, token.getSubject(), mapper.toTexto(request.texto())));
    }
}
