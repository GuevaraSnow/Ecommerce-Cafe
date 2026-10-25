package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.valueobject.FichaDeOrigen;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;
import com.uniquindio.ecommerce.application.dto.request.RegistrarTransformacionRequest;
import com.uniquindio.ecommerce.application.dto.response.FichaDeOrigenResponse;
import com.uniquindio.ecommerce.application.dto.response.TransformacionResponse;
import com.uniquindio.ecommerce.application.usecase.ConsultarFichaOrigen;
import com.uniquindio.ecommerce.application.usecase.RegistrarTransformacion;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.TransformacionMapper;
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
@RequestMapping("/api/transformaciones")
@RequiredArgsConstructor
@Tag(name = "Transformaciones", description = "Registro de Transformaciones y consulta de Ficha de Origen")
public class TransformacionController {

    private final RegistrarTransformacion registrarTransformacion;
    private final ConsultarFichaOrigen consultarFichaOrigen;
    private final TransformacionMapper mapper;

    @PostMapping
    @Operation(summary = "Registrar una Transformación", description = "Procesa un Lote de café aplicando merma y generando café tostado.")
    @ApiResponse(responseCode = "201", description = "Transformación registrada con éxito")
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o regla de dominio incumplida")
    public ResponseEntity<TransformacionResponse> registrar(@Valid @RequestBody RegistrarTransformacionRequest request) {
        Transformacion transformacion = registrarTransformacion.ejecutar(
                UUID.randomUUID().toString(),
                request.loteCodigo(),
                mapper.toCantidad(request.cantidadEntrada()),
                new PorcentajeDeMerma(request.merma()),
                request.tostadorId(),
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
    @ApiResponse(responseCode = "404", description = "No se encontró la transformación")
    public ResponseEntity<FichaDeOrigenResponse> consultarFichaOrigen(@PathVariable String id) {
        FichaDeOrigen ficha = consultarFichaOrigen.ejecutar(id);
        return ResponseEntity.ok(mapper.toFichaResponse(ficha));
    }
}