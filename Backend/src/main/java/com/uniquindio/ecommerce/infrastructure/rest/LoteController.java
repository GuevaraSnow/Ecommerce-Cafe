package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.application.dto.request.RegistrarLoteRequest;
import com.uniquindio.ecommerce.application.dto.response.LoteResponse;
import com.uniquindio.ecommerce.application.usecase.ConsultarLote;
import com.uniquindio.ecommerce.application.usecase.RegistrarLote;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.LoteMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Operaciones sobre el Lote: registrar y consultar. El controlador solo recibe, valida la forma,
 * delega en el caso de uso, mapea y responde; no captura excepciones (las traduce
 * {@code GlobalExceptionHandler}). El código del Lote lo genera el servidor, nunca el cliente.
 */
@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Registro y consulta de Lotes de café")
public class LoteController {

    private final ConsultarLote consultarLote;
    private final RegistrarLote registrarLote;
    private final LoteMapper loteMapper;

    @GetMapping("/{codigoDeLote}")
    @Operation(summary = "Consultar un Lote",
            description = "Busca un Lote por su código (formato LOT-AAAA-NNN).")
    @ApiResponse(responseCode = "200", description = "Lote encontrado")
    @ApiResponse(responseCode = "400", description = "El código no tiene el formato LOT-AAAA-NNN")
    @ApiResponse(responseCode = "404", description = "No existe un Lote con ese código")
    public ResponseEntity<LoteResponse> consultarLote(@PathVariable String codigoDeLote) {
        Lote lote = consultarLote.ejecutar(loteMapper.toCodigoDeLote(codigoDeLote));
        return ResponseEntity.ok(loteMapper.toResponse(lote));
    }

    @PostMapping
    @Operation(summary = "Registrar un Lote",
            description = "El servidor genera el código consecutivo del año (LOT-AAAA-NNN).")
    @ApiResponse(responseCode = "201", description = "Lote registrado; el Location apunta a su consulta")
    @ApiResponse(responseCode = "400", description = "Datos mal formados o regla de negocio violada")
    public ResponseEntity<LoteResponse> registrarLote(@Valid @RequestBody RegistrarLoteRequest request) {
        Lote lote = registrarLote.ejecutar(
                loteMapper.toCantidad(request.cantidad()),
                request.fincaId(),
                request.procesoDeBeneficio(),
                request.estado(),
                loteMapper.toCosecha(request.cosecha()),
                loteMapper.toVariedad(request.variedad())
        );

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigoDeLote}")
                .buildAndExpand(lote.getCodigoDeLote().valor())
                .toUri();

        return ResponseEntity.created(ubicacion).body(loteMapper.toResponse(lote));
    }
}
