package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.application.dto.request.ConsultarAsistenteRequest;
import com.uniquindio.ecommerce.application.dto.response.AsistenteResponse;
import com.uniquindio.ecommerce.application.usecase.ConsultarAsistente;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.AsistenteMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Asesor de ventas con IA: público, solo recomienda sobre el catálogo y no modifica nada. */
@RestController
@RequestMapping("/api/asistente")
@RequiredArgsConstructor
@Tag(name = "Asesor con IA", description = "Recomendaciones sobre el catálogo con ayuda de IA")
public class AsistenteController {

    private final ConsultarAsistente consultarAsistente;
    private final AsistenteMapper mapper;

    @PostMapping
    @Operation(summary = "Pregunta al asesor de ventas",
            description = "Recibe el mensaje y los últimos mensajes de la conversación; responde según el catálogo disponible.")
    @ApiResponse(responseCode = "422", description = "Mensaje inválido o el asesor no está disponible")
    public AsistenteResponse preguntar(@Valid @RequestBody ConsultarAsistenteRequest solicitud) {
        return new AsistenteResponse(
                consultarAsistente.ejecutar(solicitud.mensaje(), mapper.toHistorial(solicitud.historial())));
    }
}
