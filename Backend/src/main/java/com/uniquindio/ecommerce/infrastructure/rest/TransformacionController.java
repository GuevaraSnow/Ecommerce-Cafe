package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.valueobject.FichaDeOrigen;
import com.uniquindio.ecommerce.application.dto.request.RegistrarTransformacionRequest;
import com.uniquindio.ecommerce.application.dto.response.FichaDeOrigenResponse;
import com.uniquindio.ecommerce.application.dto.response.TransformacionResponse;
import com.uniquindio.ecommerce.application.usecase.ConsultarFichaOrigen;
import com.uniquindio.ecommerce.application.usecase.RegistrarTransformacion;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.TransformacionMapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Registro de Transformaciones y consulta de la Ficha de Origen. El controlador solo recibe, valida la forma,
 * delega en el caso de uso, mapea y responde; no captura excepciones (las traduce {@code GlobalExceptionHandler}).
 *
 * <p>El Tostador sale del token JWT de la petición, nunca del cuerpo. Registrar exige ser Tostador; consultar
 * la Ficha de Origen, solo estar autenticado.
 */
@RestController
@RequestMapping("/api/transformaciones")
@RequiredArgsConstructor
@Tag(name = "Transformaciones", description = "Registro de Transformaciones y consulta de Ficha de Origen")
@SecurityRequirement(name = "bearerAuth")
public class TransformacionController {

    private static final String TOSTADOR = "hasAuthority('TOSTADOR')";

    private final RegistrarTransformacion registrarTransformacion;
    private final ConsultarFichaOrigen consultarFichaOrigen;
    private final TransformacionMapper mapper;

    @PostMapping
    @PreAuthorize(TOSTADOR)
    @Operation(summary = "Registrar una Transformación",
            description = "Solo un Tostador: procesa un Lote de café aplicando merma y generando café tostado.")
    @ApiResponse(responseCode = "201", description = "Transformación registrada; el Location apunta a ella")
    @ApiResponse(responseCode = "400", description = "Datos mal formados")
    @ApiResponse(responseCode = "422", description = "Regla de dominio incumplida")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "El usuario no es Tostador")
    @ApiResponse(responseCode = "404", description = "No existe el Lote")
    public ResponseEntity<TransformacionResponse> registrar(@AuthenticationPrincipal Jwt token,
                                                            @Valid @RequestBody RegistrarTransformacionRequest request) {
        Transformacion transformacion = registrarTransformacion.ejecutar(
                UUID.randomUUID().toString(),
                mapper.toCodigoDeLote(request.loteCodigo()),
                mapper.toCantidad(request.cantidadEntrada()),
                mapper.toMerma(request.merma()),
                token.getSubject(),
                request.fecha(),
                request.perfilTuesteResultante()
        );

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transformacion.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(mapper.toResponse(transformacion));
    }

    @GetMapping("/{id}/ficha-origen")
    @Operation(summary = "Consultar Ficha de Origen", description = "Consolida la trazabilidad real (Finca, Cosecha, Lote y Transformación).")
    @ApiResponse(responseCode = "200", description = "Ficha de origen encontrada")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No se encontró la transformación, su Lote o su Finca")
    public ResponseEntity<FichaDeOrigenResponse> consultarFichaOrigen(@PathVariable String id) {
        FichaDeOrigen ficha = consultarFichaOrigen.ejecutar(id);
        return ResponseEntity.ok(mapper.toFichaResponse(ficha));
    }
}
