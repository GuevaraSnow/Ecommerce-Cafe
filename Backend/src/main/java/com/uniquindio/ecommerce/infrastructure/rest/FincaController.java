package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.application.dto.request.RegistrarFincaRequest;
import com.uniquindio.ecommerce.application.dto.response.FincaResponse;
import com.uniquindio.ecommerce.application.usecase.ConsultarFinca;
import com.uniquindio.ecommerce.application.usecase.RegistrarFinca;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.FincaMapper;
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
 * Registro y consulta de Fincas. El controlador solo recibe, valida la forma, delega en el caso de uso, mapea
 * y responde; no captura excepciones (las traduce {@code GlobalExceptionHandler}).
 *
 * <p>El propietario de la Finca sale del token JWT de la petición, nunca de un encabezado ni del cuerpo.
 * Registrar exige ser Caficultor; consultar, solo estar autenticado.
 */
@RestController
@RequestMapping("/api/fincas")
@RequiredArgsConstructor
@Tag(name = "Fincas", description = "Registro y consulta de Fincas")
@SecurityRequirement(name = "bearerAuth")
public class FincaController {

    private static final String CAFICULTOR = "hasAuthority('CAFICULTOR')";

    private final RegistrarFinca registrarFinca;
    private final ConsultarFinca consultarFinca;
    private final FincaMapper mapper;

    @PostMapping
    @PreAuthorize(CAFICULTOR)
    @Operation(summary = "Registrar una Finca", description = "Solo un Caficultor; la Finca queda a su nombre.")
    @ApiResponse(responseCode = "201", description = "Finca registrada; el Location apunta a su consulta")
    @ApiResponse(responseCode = "400", description = "Datos de finca inválidos")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "403", description = "El usuario no es Caficultor")
    public ResponseEntity<FincaResponse> registrar(@AuthenticationPrincipal Jwt token,
                                                   @Valid @RequestBody RegistrarFincaRequest request) {
        Finca finca = registrarFinca.ejecutar(
                UUID.randomUUID().toString(),
                token.getSubject(),
                request.nombre(),
                mapper.toUbicacion(request.ubicacion()),
                mapper.toAltitud(request.altitud())
        );

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(finca.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(mapper.toResponse(finca));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una Finca por ID")
    @ApiResponse(responseCode = "200", description = "Finca encontrada")
    @ApiResponse(responseCode = "401", description = "Sin token o token inválido")
    @ApiResponse(responseCode = "404", description = "No existe la finca")
    public FincaResponse consultar(@PathVariable String id) {
        return mapper.toResponse(consultarFinca.ejecutar(id));
    }
}
