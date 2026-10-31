package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import com.uniquindio.ecommerce.application.dto.response.PaginaResponse;
import com.uniquindio.ecommerce.application.dto.response.PresentacionResponse;
import com.uniquindio.ecommerce.application.dto.response.SugerenciaResponse;
import com.uniquindio.ecommerce.application.usecase.BuscarPresentaciones;
import com.uniquindio.ecommerce.application.usecase.ConsultarPresentacion;
import com.uniquindio.ecommerce.application.usecase.SugerirPresentaciones;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.PresentacionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consultas de Presentaciones, separadas de los comandos: no modifican nada y son públicas.
 * Solo listan lo que está disponible para la venta.
 */
@RestController
@RequestMapping("/api/presentaciones")
@RequiredArgsConstructor
@Tag(name = "Catálogo de Presentaciones", description = "Consulta, búsqueda y sugerencias de Presentaciones")
public class PresentacionConsultaController {

    private final ConsultarPresentacion consultarPresentacion;
    private final BuscarPresentaciones buscarPresentaciones;
    private final SugerirPresentaciones sugerirPresentaciones;
    private final PresentacionMapper mapper;

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una Presentación", description = "Incluye galería, precio vigente y descuento.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse consultar(@PathVariable String id) {
        return mapper.toResponse(consultarPresentacion.ejecutar(id));
    }

    @GetMapping
    @Operation(summary = "Buscar Presentaciones disponibles",
            description = "Filtros opcionales por título, tipo, vendedor, rango de precio normal y calificación "
                    + "mínima (promedio de reseñas, de 1 a 5); paginado y ordenado por título. "
                    + "La primera página es la 0.")
    @ApiResponse(responseCode = "400", description = "Filtro o paginación inválidos")
    public PaginaResponse<PresentacionResponse> buscar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) TipoDePresentacion tipo,
            @RequestParam(required = false) String vendedorId,
            @RequestParam(required = false) Double precioMinimo,
            @RequestParam(required = false) Double precioMaximo,
            @RequestParam(required = false) Double calificacionMinima,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
            @Max(value = 50, message = "El tamaño de página no puede superar 50") int tamano) {
        return mapper.toPaginaResponse(buscarPresentaciones.ejecutar(
                mapper.toFiltro(titulo, tipo, vendedorId, precioMinimo, precioMaximo, calificacionMinima), pagina, tamano));
    }

    @GetMapping("/sugerencias")
    @Operation(summary = "Búsqueda predictiva por nombre",
            description = "Presentaciones disponibles cuyo título contiene el texto, para sugerir mientras se escribe.")
    @ApiResponse(responseCode = "400", description = "Falta el texto o el límite es inválido")
    public List<SugerenciaResponse> sugerir(
            @RequestParam @NotBlank(message = "El texto de búsqueda es obligatorio") String texto,
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "El límite debe ser al menos 1")
            @Max(value = 10, message = "El límite no puede superar 10") int limite) {
        return sugerirPresentaciones.ejecutar(texto, limite).stream().map(mapper::toSugerencia).toList();
    }
}
