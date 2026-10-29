package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.application.dto.response.ResenaResponse;
import com.uniquindio.ecommerce.application.usecase.ConsultarResenasDePresentacion;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.ResenaMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consultas de Reseñas, separadas de los comandos: no modifican nada y son públicas, como el catálogo. */
@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
@Tag(name = "Reseñas de una Presentación", description = "Consulta pública de reseñas")
public class ResenaConsultaController {

    private final ConsultarResenasDePresentacion consultarResenasDePresentacion;
    private final ResenaMapper mapper;

    @GetMapping
    @Operation(summary = "Reseñas de una Presentación",
            description = "De la más reciente a la más antigua, con la respuesta del vendedor si la hay.")
    @ApiResponse(responseCode = "400", description = "Falta la Presentación")
    public List<ResenaResponse> deUnaPresentacion(
            @RequestParam @NotBlank(message = "La Presentacion es obligatoria") String presentacionId) {
        return mapper.toResponses(consultarResenasDePresentacion.ejecutar(presentacionId));
    }
}
