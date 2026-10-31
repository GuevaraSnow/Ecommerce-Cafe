package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.application.dto.response.CompraResponse;
import com.uniquindio.ecommerce.application.usecase.ConsultarCompra;
import com.uniquindio.ecommerce.application.usecase.ConsultarHistorialDeCompras;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.CompraMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consultas de Compras, separadas de los comandos: no modifican nada. El historial es siempre el del
 * Comprador del token; el detalle lo ve su Comprador o un vendedor con alguna Presentación en la Compra.
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@Tag(name = "Historial de Compras", description = "Historial y detalle de las Compras")
@SecurityRequirement(name = "bearerAuth")
public class CompraConsultaController {

    private static final String COMPRADOR = "hasAuthority('COMPRADOR')";
    private static final String DUENO_O_VENDEDOR = "@autorizacionDeCompra.esDelComprador(#id, authentication)"
            + " or (hasAnyAuthority('CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS')"
            + " and @autorizacionDeCompra.esVendedorDe(#id, authentication))";

    private final ConsultarHistorialDeCompras consultarHistorialDeCompras;
    private final ConsultarCompra consultarCompra;
    private final CompraMapper mapper;

    @GetMapping
    @PreAuthorize(COMPRADOR)
    @Operation(summary = "Historial de Compras del Comprador",
            description = "Todas sus Compras, de la más reciente a la más antigua, incluido el carrito abierto.")
    public List<CompraResponse> historial(@AuthenticationPrincipal Jwt token) {
        return mapper.toResponses(consultarHistorialDeCompras.ejecutar(token.getSubject()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(DUENO_O_VENDEDOR)
    @Operation(summary = "Detalle de una Compra", description = "Solo para su Comprador o un vendedor de la Compra.")
    @ApiResponse(responseCode = "404", description = "No existe la Compra")
    public CompraResponse consultar(@PathVariable String id) {
        return mapper.toResponse(consultarCompra.ejecutar(id));
    }
}
