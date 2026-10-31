package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.application.dto.request.SolicitarReembolsoRequest;
import com.uniquindio.ecommerce.application.dto.response.ReembolsoResponse;
import com.uniquindio.ecommerce.application.usecase.AprobarReembolso;
import com.uniquindio.ecommerce.application.usecase.RechazarReembolso;
import com.uniquindio.ecommerce.application.usecase.SolicitarReembolso;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.ReembolsoMapper;
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
 * Operaciones sobre Solicitudes de Reembolso: un endpoint por operación del dominio. El controlador solo
 * recibe, valida la forma, delega en el caso de uso, mapea y responde.
 *
 * <p>Quién actúa sale del token. Que la Compra sea del Comprador, que esté entregada y dentro de 48 h, y que
 * solo el vendedor dueño de la Presentación resuelva, lo valida el dominio (responde 422 si no se cumple).
 */
@RestController
@RequestMapping("/api/reembolsos")
@RequiredArgsConstructor
@Tag(name = "Reembolsos", description = "Solicitud y resolución de reembolsos por Presentación")
@SecurityRequirement(name = "bearerAuth")
public class ReembolsoController {

    private static final String COMPRADOR = "hasAuthority('COMPRADOR')";
    private static final String VENDEDOR = "hasAnyAuthority('CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS')";

    private final SolicitarReembolso solicitarReembolso;
    private final AprobarReembolso aprobarReembolso;
    private final RechazarReembolso rechazarReembolso;
    private final ReembolsoMapper mapper;

    @PostMapping
    @PreAuthorize(COMPRADOR)
    @Operation(summary = "Solicitar el reembolso de una Presentación",
            description = "Solo sobre una Compra propia, entregada y dentro de las 48 h siguientes a la entrega.")
    @ApiResponse(responseCode = "201", description = "Solicitud creada; el Location apunta a ella")
    @ApiResponse(responseCode = "400", description = "Datos mal formados")
    @ApiResponse(responseCode = "422", description = "Fuera de plazo, compra ajena o ya solicitado")
    @ApiResponse(responseCode = "404", description = "No existe la Compra o la Presentación")
    public ResponseEntity<ReembolsoResponse> solicitar(@AuthenticationPrincipal Jwt token,
                                                       @Valid @RequestBody SolicitarReembolsoRequest request) {
        SolicitudReembolso solicitud = solicitarReembolso.ejecutar(UUID.randomUUID().toString(),
                request.compraId(), token.getSubject(), request.presentacionId(), request.motivo());
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/reembolsos/{id}").buildAndExpand(solicitud.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toResponse(solicitud));
    }

    @PutMapping("/{id}/aprobar")
    @PreAuthorize(VENDEDOR)
    @Operation(summary = "Aprobar una solicitud de reembolso",
            description = "Solo el vendedor dueño de la Presentación. Si se aprueban todas, la Compra queda REEMBOLSADA.")
    @ApiResponse(responseCode = "422", description = "No es el vendedor dueño o la solicitud ya se resolvió")
    @ApiResponse(responseCode = "404", description = "No existe la solicitud")
    public ReembolsoResponse aprobar(@PathVariable String id, @AuthenticationPrincipal Jwt token) {
        return mapper.toResponse(aprobarReembolso.ejecutar(id, token.getSubject()));
    }

    @PutMapping("/{id}/rechazar")
    @PreAuthorize(VENDEDOR)
    @Operation(summary = "Rechazar una solicitud de reembolso", description = "Solo el vendedor dueño de la Presentación.")
    @ApiResponse(responseCode = "422", description = "No es el vendedor dueño o la solicitud ya se resolvió")
    @ApiResponse(responseCode = "404", description = "No existe la solicitud")
    public ReembolsoResponse rechazar(@PathVariable String id, @AuthenticationPrincipal Jwt token) {
        return mapper.toResponse(rechazarReembolso.ejecutar(id, token.getSubject()));
    }
}
