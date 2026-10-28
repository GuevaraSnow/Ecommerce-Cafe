package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.application.dto.request.AgregarAlCarritoRequest;
import com.uniquindio.ecommerce.application.dto.request.DireccionDeEnvioRequest;
import com.uniquindio.ecommerce.application.dto.response.CompraResponse;
import com.uniquindio.ecommerce.application.usecase.AgregarAlCarrito;
import com.uniquindio.ecommerce.application.usecase.CancelarCompra;
import com.uniquindio.ecommerce.application.usecase.ConfirmarCompra;
import com.uniquindio.ecommerce.application.usecase.CrearCarrito;
import com.uniquindio.ecommerce.application.usecase.MarcarEntregada;
import com.uniquindio.ecommerce.application.usecase.MarcarEnviada;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.CompraMapper;
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
 * Operaciones que modifican una Compra: un endpoint por operación del dominio. El controlador solo
 * recibe, valida la forma, delega en el caso de uso, mapea y responde; no captura excepciones
 * (las traduce {@code GlobalExceptionHandler}).
 *
 * <p>El Comprador sale del token JWT, nunca del cuerpo. El carrito solo lo toca su Comprador; enviar
 * y entregar lo hace un vendedor que tenga alguna Presentación en la Compra.
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@Tag(name = "Compras", description = "Carrito y ciclo de vida de la Compra")
@SecurityRequirement(name = "bearerAuth")
public class CompraController {

    private static final String COMPRADOR = "hasAuthority('COMPRADOR')";
    private static final String ES_DEL_COMPRADOR = "@autorizacionDeCompra.esDelComprador(#id, authentication)";
    private static final String ES_VENDEDOR = "hasAnyAuthority('CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS')"
            + " and @autorizacionDeCompra.esVendedorDe(#id, authentication)";

    private final CrearCarrito crearCarrito;
    private final AgregarAlCarrito agregarAlCarrito;
    private final ConfirmarCompra confirmarCompra;
    private final CancelarCompra cancelarCompra;
    private final MarcarEnviada marcarEnviada;
    private final MarcarEntregada marcarEntregada;
    private final CompraMapper mapper;

    @PostMapping
    @PreAuthorize(COMPRADOR)
    @Operation(summary = "Abrir un carrito", description = "Crea una Compra PENDIENTE vacía para el Comprador del token.")
    @ApiResponse(responseCode = "201", description = "Carrito creado; el Location apunta a su detalle")
    public ResponseEntity<CompraResponse> crearCarrito(@AuthenticationPrincipal Jwt token) {
        Compra carrito = crearCarrito.ejecutar(UUID.randomUUID().toString(), token.getSubject());
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/compras/{id}").buildAndExpand(carrito.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toResponse(carrito));
    }

    @PostMapping("/{id}/presentaciones")
    @PreAuthorize(COMPRADOR + " and " + ES_DEL_COMPRADOR)
    @Operation(summary = "Agregar una Presentación al carrito",
            description = "Congela el precio vigente; el stock se verifica pero se descuenta al confirmar.")
    @ApiResponse(responseCode = "400", description = "Datos mal formados, stock insuficiente o la Compra ya no es un carrito")
    @ApiResponse(responseCode = "404", description = "No existe la Compra o la Presentación")
    public CompraResponse agregarAlCarrito(@PathVariable String id,
                                           @Valid @RequestBody AgregarAlCarritoRequest request) {
        return mapper.toResponse(agregarAlCarrito.ejecutar(id, request.presentacionId(),
                mapper.toCantidad(request.cantidad())));
    }

    @PutMapping("/{id}/confirmar")
    @PreAuthorize(COMPRADOR + " and " + ES_DEL_COMPRADOR)
    @Operation(summary = "Confirmar la Compra",
            description = "Fija la dirección de envío, descuenta el stock (todo o nada) y envía el correo de confirmación.")
    @ApiResponse(responseCode = "400", description = "Dirección inválida, carrito vacío, stock insuficiente o ya confirmada")
    @ApiResponse(responseCode = "404", description = "No existe la Compra")
    public CompraResponse confirmar(@PathVariable String id, @Valid @RequestBody DireccionDeEnvioRequest request) {
        return mapper.toResponse(confirmarCompra.ejecutar(id, mapper.toDireccionDeEnvio(request)));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize(COMPRADOR + " and " + ES_DEL_COMPRADOR)
    @Operation(summary = "Cancelar la Compra", description = "Solo antes del envío; si ya estaba confirmada repone el stock.")
    @ApiResponse(responseCode = "400", description = "La Compra ya fue enviada o cancelada")
    @ApiResponse(responseCode = "404", description = "No existe la Compra")
    public CompraResponse cancelar(@PathVariable String id) {
        return mapper.toResponse(cancelarCompra.ejecutar(id));
    }

    @PutMapping("/{id}/enviar")
    @PreAuthorize(ES_VENDEDOR)
    @Operation(summary = "Marcar la Compra como enviada", description = "Solo una Compra confirmada.")
    @ApiResponse(responseCode = "400", description = "La Compra no está confirmada")
    @ApiResponse(responseCode = "404", description = "No existe la Compra")
    public CompraResponse enviar(@PathVariable String id) {
        return mapper.toResponse(marcarEnviada.ejecutar(id));
    }

    @PutMapping("/{id}/entregar")
    @PreAuthorize(ES_VENDEDOR)
    @Operation(summary = "Marcar la Compra como entregada",
            description = "Solo una Compra enviada; desde ese momento corren las 48 h para pedir reembolso.")
    @ApiResponse(responseCode = "400", description = "La Compra no fue enviada")
    @ApiResponse(responseCode = "404", description = "No existe la Compra")
    public CompraResponse entregar(@PathVariable String id) {
        return mapper.toResponse(marcarEntregada.ejecutar(id));
    }
}
