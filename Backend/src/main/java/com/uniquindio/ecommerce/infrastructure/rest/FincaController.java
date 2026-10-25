package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.application.dto.request.RegistrarFincaRequest;
import com.uniquindio.ecommerce.application.dto.response.FincaResponse;
import com.uniquindio.ecommerce.application.usecase.RegistrarFinca;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.FincaMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/fincas")
@RequiredArgsConstructor
@Tag(name = "Fincas", description = "Registro y consulta de Fincas")
public class FincaController {

    private static final String CAFICULTOR_HEADER = "X-Caficultor-Id";

    private final RegistrarFinca registrarFinca;
    private final FincaRepositorio fincaRepositorio;
    private final FincaMapper mapper;

    @PostMapping
    @Operation(summary = "Registrar una Finca", description = "Registra una nueva finca asociándola al caficultor autenticado.")
    @ApiResponse(responseCode = "201", description = "Finca registrada con éxito")
    @ApiResponse(responseCode = "400", description = "Datos de finca inválidos")
    public ResponseEntity<FincaResponse> registrar(
            @RequestHeader(value = CAFICULTOR_HEADER, required = false) String caficultorIdHeader,
            @Valid @RequestBody RegistrarFincaRequest request) {

        String propietarioId = (caficultorIdHeader != null && !caficultorIdHeader.isBlank())
                ? caficultorIdHeader
                : "caficultor-default";

        Finca finca = registrarFinca.ejecutar(
                UUID.randomUUID().toString(),
                propietarioId,
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
    @ApiResponse(responseCode = "404", description = "No existe la finca")
    public ResponseEntity<FincaResponse> buscarPorId(@PathVariable String id) {
        Finca finca = fincaRepositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaDominioException("No se encontró la finca con id: " + id));
        return ResponseEntity.ok(mapper.toResponse(finca));
    }
}